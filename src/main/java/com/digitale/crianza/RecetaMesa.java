package com.digitale.crianza;

import com.digitale.datos.AlmacenJugadores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Lógica de la Mesa de Crianza v2 (arrastre + experimentación):
 * - Slot 0: Huevo Digitama_* (obligatorio).
 * - Slot 1: Dato Dato_Incubacion_* (opcional, fija especie si es del pool).
 * - Slots 2-3: Extras libres (minerales, comida, ingredientes).
 *
 * Con receta conocida + dato válido + extras que cubren ingredientes: 100%.
 * Sin receta: probabilidad por tier del huevo (fallo = Burpmon).
 */
public final class RecetaMesa {

    private RecetaMesa() {
    }

    private static final Random RNG = new Random();

    public static class Extra {
        public String id;
        public int qty;
        public short slot;
    }

    public static class Ingrediente {
        public int qty;
        /** Nombres aceptados normalizados (principal + alternativas "o"). */
        public List<String> nombres = new ArrayList<>();
    }

    public static class Mezcla {
        public boolean valida;
        public String error = "";
        public DigitamaDef def;
        public short huevoSlot = -1;
        public short datoSlot = -1;
        public String datoBebe;
        public boolean datoValido;
        public List<Extra> extras = new ArrayList<>();
        public boolean recetaConocida;
        public boolean extrasCubren;
        public String especieObjetivo = "";
        public int exitoPct = 0;
        public String detalle = "";
    }

    /** "5x DigiManzana" -> {qty 5, nombres [digimanzana]}; null si no parsea. */
    static Ingrediente parseIngrediente(String crudo) {
        if (crudo == null) return null;
        String t = crudo.trim();
        int x = t.indexOf('x');
        if (x <= 0) return null;
        int qty;
        try {
            qty = Integer.parseInt(t.substring(0, x).trim());
        } catch (NumberFormatException e) {
            return null;
        }
        String resto = t.substring(x + 1).trim();
        if (resto.isEmpty() || qty <= 0) return null;
        Ingrediente ing = new Ingrediente();
        ing.qty = qty;
        // Alternativas entre paréntesis: "Piedra de Fuego (o Carbon)".
        String principal = resto;
        List<String> alts = new ArrayList<>();
        int pa = resto.indexOf('(');
        int pc = resto.indexOf(')');
        if (pa >= 0 && pc > pa) {
            principal = resto.substring(0, pa).trim();
            String dentro = resto.substring(pa + 1, pc).trim();
            // "o Carbon" / "Carbon o Lava" / "Kelp"
            for (String parte : dentro.split("\\s+o\\s+|\\s*,\\s*")) {
                String p = parte.trim().replaceFirst("^(o|O)\\s+", "");
                if (!p.isEmpty()) alts.add(normalizar(p));
            }
        }
        if (!principal.isEmpty()) ing.nombres.add(normalizar(principal));
        ing.nombres.addAll(alts);
        return ing.nombres.isEmpty() ? null : ing;
    }

    static String normalizar(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(java.util.Locale.ROOT).replace('_', ' ')
            .replaceAll("\\s+", " ");
    }

    static int basePorTier(String tier) {
        if (tier == null) return 30;
        switch (tier.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "basico":
            case "básico":
                return 60;
            case "medio":
                return 40;
            case "avanzado":
                return 25;
            case "legendario":
                return 12;
            default:
                return 30;
        }
    }

    /** Fuente de slots (permite probar sin servidor). */
    public interface Lector {
        String id(short slot);

        int qty(short slot);

        short capacidad();

        /** Consume qty del slot virtual (por defecto no hace nada). */
        default void consumir(short slot, int qty) {
        }
    }

    /** Lector sobre el contenedor real de la mesa. */
    public static Lector lectorDe(MesaContainer cont) {
        return new Lector() {
            @Override
            public String id(short slot) {
                try {
                    return MesaContainer.cortoDe(cont.getItemStack(slot));
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public int qty(short slot) {
                try {
                    return MesaContainer.cantidadDe(cont.getItemStack(slot));
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public short capacidad() {
                return MesaContainer.CAPACIDAD;
            }

            @Override
            public void consumir(short slot, int qty) {
                try {
                    cont.removeItemStackFromSlot(slot, qty);
                } catch (Exception ignored) {
                }
            }
        };
    }

    /** Lector sobre un contenedor cualquiera (ej. input de bench nativo). */
    public static Lector lectorDe(com.hypixel.hytale.server.core.inventory.container.ItemContainer cont) {
        return new Lector() {
            @Override
            public String id(short slot) {
                try {
                    return MesaContainer.cortoDe(cont.getItemStack(slot));
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public int qty(short slot) {
                try {
                    return MesaContainer.cantidadDe(cont.getItemStack(slot));
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public short capacidad() {
                try {
                    return cont.getCapacity();
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public void consumir(short slot, int qty) {
                try {
                    cont.removeItemStackFromSlot(slot, qty);
                } catch (Exception ignored) {
                }
            }
        };
    }

    /**
     * Lector combinado input+fuel del bench nativo (huevo en input,
     * dato y extras donde sea). Slots virtuales: 0..inCap-1 = input,
     * resto = fuel. El consumo se enruta al contenedor real.
     */
    public static Lector lectorBench(
        final com.hypixel.hytale.server.core.inventory.container.ItemContainer input,
        final com.hypixel.hytale.server.core.inventory.container.ItemContainer fuel) {
        return new Lector() {
            private short capInput() {
                try {
                    return input != null ? input.getCapacity() : 0;
                } catch (Exception e) {
                    return 0;
                }
            }

            private short capFuel() {
                try {
                    return fuel != null ? fuel.getCapacity() : 0;
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public String id(short slot) {
                try {
                    if (slot < capInput()) return MesaContainer.cortoDe(input.getItemStack(slot));
                    return MesaContainer.cortoDe(fuel.getItemStack((short) (slot - capInput())));
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public int qty(short slot) {
                try {
                    if (slot < capInput()) return MesaContainer.cantidadDe(input.getItemStack(slot));
                    return MesaContainer.cantidadDe(fuel.getItemStack((short) (slot - capInput())));
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public short capacidad() {
                return (short) (capInput() + capFuel());
            }

            @Override
            public void consumir(short slot, int qty) {
                try {
                    if (slot < capInput()) input.removeItemStackFromSlot(slot, qty);
                    else fuel.removeItemStackFromSlot((short) (slot - capInput()), qty);
                } catch (Exception ignored) {
                }
            }
        };
    }

    /** Evalúa los 4 slots sin consumir nada (para "Probar" y "Combinar"). */
    public static Mezcla evaluar(MesaContainer cont, AlmacenJugadores.DatosJugador datos) {
        if (cont == null) {
            Mezcla m = new Mezcla();
            m.error = "Mesa no disponible.";
            return m;
        }
        return evaluar(lectorDe(cont), datos);
    }

    public static Mezcla evaluar(Lector slots, AlmacenJugadores.DatosJugador datos) {
        Mezcla m = new Mezcla();
        if (slots == null) {
            m.error = "Mesa no disponible.";
            return m;
        }
        String huevoId = null;
        String datoId = null;
        // Scan por contenido: primer Digitama_* = huevo, primer Dato_Incubacion_* = dato.
        for (short s = 0; s < slots.capacidad(); s++) {
            String id = slots.id(s);
            if (id == null) continue;
            if (huevoId == null && id.startsWith("Digitama_")) {
                huevoId = id;
                m.huevoSlot = s;
            } else if (datoId == null && id.startsWith("Dato_Incubacion_")) {
                datoId = id;
                m.datoSlot = s;
            }
        }
        if (huevoId == null || !huevoId.startsWith("Digitama_")) {
            m.error = "Falta un huevo Digitama en la mesa.";
            return m;
        }
        DigitamaDef def = SistemaIncubacion.porItem(huevoId);
        if (def == null || SistemaIncubacion.esFallback(def)
            || "Digitama_Desconocido".equals(huevoId)) {
            m.error = "Huevo no reconocido (" + huevoId + ").";
            return m;
        }
        m.def = def;

        if (datoId != null) {
            String bebe = datoId.substring("Dato_Incubacion_".length());
            if (!bebe.isEmpty()) m.datoBebe = bebe;
        }
        try {
            m.datoValido = m.datoBebe != null && def.pool().contains(m.datoBebe);
        } catch (Exception e) {
            m.datoValido = false;
        }
        if (m.datoBebe != null && !m.datoValido) {
            m.error = "Ese dato (" + m.datoBebe + ") no es de este huevo.";
            return m;
        }

        for (short s = 0; s < slots.capacidad(); s++) {
            if (s == m.huevoSlot || s == m.datoSlot) continue;
            String id = slots.id(s);
            if (id == null) continue;
            Extra e = new Extra();
            e.id = id;
            e.qty = slots.qty(s);
            e.slot = s;
            m.extras.add(e);
        }

        m.recetaConocida = datos != null && datos.tieneReceta(def.especie);
        m.extrasCubren = cubrenIngredientes(def, m.extras);

        if (m.datoValido) {
            m.especieObjetivo = m.datoBebe;
        } else {
            List<String> pool = def.pool();
            m.especieObjetivo = pool.isEmpty() ? def.especie
                : pool.get(RNG.nextInt(pool.size()));
        }

        int base = basePorTier(def.tier);
        if (m.datoValido && m.recetaConocida && m.extrasCubren) {
            m.exitoPct = 100;
        } else {
            int pct = base;
            if (!m.datoValido) pct -= 15;
            pct += Math.min(15, 5 * m.extras.size());
            if (pct > 95) pct = 95;
            if (pct < 5) pct = 5;
            m.exitoPct = pct;
        }

        StringBuilder det = new StringBuilder();
        det.append(def.nombre).append(" -> ").append(m.especieObjetivo);
        det.append(" | Exito ").append(m.exitoPct).append("%");
        if (m.exitoPct >= 100) {
            det.append(" (receta completa)");
        } else {
            List<String> faltan = new ArrayList<>();
            if (!m.datoValido) faltan.add("dato del pool");
            if (!m.recetaConocida) faltan.add("receta bloqueada");
            if (!m.extrasCubren) faltan.add("ingredientes");
            if (!faltan.isEmpty()) det.append(" [falta: ").append(String.join(", ", faltan)).append("]");
            det.append(" [fallo = Burpmon]");
        }
        m.detalle = det.toString();
        m.valida = true;
        return m;
    }

    /**
     * Cuánto consumir de cada slot extra al combinar: si la receta está
     * completa, las cantidades exactas; si es experimento, 1 por slot.
     */
    public static Map<Short, Integer> consumoPara(Mezcla m) {
        Map<Short, Integer> out = new HashMap<>();
        if (m == null || !m.valida) return out;
        boolean completa = m.exitoPct >= 100;
        if (!completa) {
            for (Extra e : m.extras) out.put(e.slot, 1);
            return out;
        }
        // Receta completa: repartir necesidades por slots en orden.
        List<Ingrediente> reqs = new ArrayList<>();
        if (m.def.ingredientes != null) {
            for (String crudo : m.def.ingredientes) {
                Ingrediente ing = parseIngrediente(crudo);
                if (ing != null) reqs.add(ing);
            }
        }
        Map<Ingrediente, Integer> pendiente = new HashMap<>();
        for (Ingrediente ing : reqs) pendiente.put(ing, ing.qty);
        for (Extra e : m.extras) {
            String n = normalizar(e.id);
            int tomado = 0;
            for (Ingrediente ing : reqs) {
                int falta = pendiente.getOrDefault(ing, 0);
                if (falta <= 0 || !ing.nombres.contains(n)) continue;
                int usar = Math.min(falta, e.qty - tomado);
                tomado += usar;
                pendiente.put(ing, falta - usar);
                if (tomado >= e.qty) break;
            }
            if (tomado > 0) out.put(e.slot, tomado);
        }
        return out;
    }

    private static boolean cubrenIngredientes(DigitamaDef def, List<Extra> extras) {
        if (def.ingredientes == null || def.ingredientes.length == 0) return true;
        Map<String, Integer> disp = new HashMap<>();
        for (Extra e : extras) {
            String n = normalizar(e.id);
            disp.put(n, disp.getOrDefault(n, 0) + e.qty);
        }
        for (String crudo : def.ingredientes) {
            Ingrediente ing = parseIngrediente(crudo);
            if (ing == null) continue; // línea decorativa: no bloquea
            boolean ok = false;
            for (String nombre : ing.nombres) {
                if (disp.getOrDefault(nombre, 0) >= ing.qty) {
                    ok = true;
                    break;
                }
            }
            if (!ok) return false;
        }
        return true;
    }
}
