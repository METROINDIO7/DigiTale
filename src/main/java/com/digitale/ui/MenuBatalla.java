package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.sistema.SistemaBatalla;
import com.digitale.sistema.SistemaPaseo;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Menú de combate por teclado sobre el HUD (sin Page):
 * W/S mueven cursor, ESPACIO acepta, Q retrocede.
 * Todo aquí corre en el hilo del mundo.
 */
public final class MenuBatalla {

    private MenuBatalla() {
    }

    private static final Logger LOGGER = Logger.getLogger(MenuBatalla.class.getName());

    private static final EstadoCombate.Orden[] SKILLS = {
        EstadoCombate.Orden.SKILL_1, EstadoCombate.Orden.SKILL_2, EstadoCombate.Orden.SKILL_3,
        EstadoCombate.Orden.SKILL_4, EstadoCombate.Orden.SKILL_5, EstadoCombate.Orden.SKILL_6,
        EstadoCombate.Orden.SKILL_7
    };

    /** Líder actual (lado A del combate). */
    private static DatoDigimon liderDe(DatosJugador d) {
        EstadoCombate c = d.combate;
        if (c != null && c.party != null && !c.party.isEmpty()) return c.party.get(0);
        return d.getA();
    }
    private static final int[] COSTES = {20, 50, 100, 60, 90, 120, 150};

    /** Abrir menú al iniciar combate. */
    public static void abrir(@Nonnull PlayerRef playerRef,
                             @Nonnull Ref<EntityStore> ref,
                             @Nonnull Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;
        d.menuAbierto = true;
        d.menuNivel = 0;
        d.menuIdx = 0;
        d.menuSub = "";
        d.menuLastJump = false;
        d.menuLastDir = "";
        DigiBattleHud.mostrar(playerRef, ref, store);
        DigiBattleHud.actualizar(playerRef, ref, store);
    }

    /** Cerrar menú (Q en raíz tras FIN, huida exitosa). Recoge formación. */
    public static void cerrar(@Nonnull PlayerRef playerRef,
                              @Nonnull Ref<EntityStore> ref,
                              @Nonnull Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d != null) {
            d.menuAbierto = false;
            d.menuNivel = 0;
            d.menuIdx = 0;
            d.menuSub = "";
        }
        SistemaPaseo.recogerBatalla(playerRef, ref, store);
        DigiBattleHud.ocultar(playerRef, ref, store);
    }

    /** Mover cursor (dir "up"/"down"). */
    public static void mover(@Nonnull PlayerRef playerRef,
                             @Nonnull Ref<EntityStore> ref,
                             @Nonnull Store<EntityStore> store,
                             String dir) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null || !d.menuAbierto) return;
        List<String> items = listaItems(d);
        if (items.isEmpty()) return;
        if ("up".equals(dir)) {
            d.menuIdx = (d.menuIdx - 1 + items.size()) % items.size();
        } else {
            d.menuIdx = (d.menuIdx + 1) % items.size();
        }
        DigiBattleHud.actualizar(playerRef, ref, store);
    }

    /** Aceptar opción actual. */
    public static void aceptar(@Nonnull PlayerRef playerRef,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null || !d.menuAbierto) return;
        EstadoCombate c = d.combate;
        if (c == null) {
            cerrar(playerRef, ref, store);
            return;
        }
        List<String> items = listaItems(d);
        if (d.menuIdx < 0 || d.menuIdx >= items.size()) return;

        if (d.menuNivel == 0) {
            switch (d.menuIdx) {
                case 0 -> { d.menuNivel = 1; d.menuSub = "combate"; d.menuIdx = 0; }
                case 1 -> { d.menuNivel = 1; d.menuSub = "equipo"; d.menuIdx = 0; }
                case 2 -> { d.menuNivel = 1; d.menuSub = "tactica"; d.menuIdx = 0; }
                case 3 -> { d.menuNivel = 1; d.menuSub = "inventario"; d.menuIdx = 0; }
                case 4 -> {
                    if (SistemaBatalla.huir(c)) {
                        guardarFin(ref, store, d);
                        cerrar(playerRef, ref, store);
                        return;
                    }
                }
                default -> { }
            }
            DigiBattleHud.actualizar(playerRef, ref, store);
            return;
        }

        switch (d.menuSub) {
            case "combate" -> {
                DatoDigimon lider = liderDe(d);
                java.util.List<DatoDigimon.Movimiento> movs =
                    lider != null ? lider.movimientosEquipados() : java.util.Collections.emptyList();
                if (d.menuIdx >= 0 && d.menuIdx < movs.size()) {
                    SistemaBatalla.ejecutarMovimiento(c, lider, movs.get(d.menuIdx));
                } else {
                    SistemaBatalla.pasarTurno(c);
                }
                trasAccion(playerRef, ref, store, d, c);
            }
            case "equipo" -> {
                if (!SistemaBatalla.intercambiarLider(c, d, d.menuIdx)) {
                    c.logear("No puedes sacar a ese (dormido, KO o ya en campo).");
                    DigiBattleHud.actualizar(playerRef, ref, store);
                } else {
                    SistemaBatalla.pasarTurno(c);
                    trasAccion(playerRef, ref, store, d, c);
                }
            }
            case "tactica" -> {
                switch (d.menuIdx) {
                    case 0 -> { c.tacticaA = ciclar(c.tacticaA); c.logear("Tactica A -> " + c.tacticaA); }
                    case 1 -> { c.tacticaB = ciclar(c.tacticaB); c.logear("Tactica B -> " + c.tacticaB); }
                    case 2 -> { c.tacticaC = ciclar(c.tacticaC); c.logear("Tactica C -> " + c.tacticaC); }
                    default -> { }
                }
                DigiBattleHud.actualizar(playerRef, ref, store);
            }
            default -> DigiBattleHud.actualizar(playerRef, ref, store);
        }
    }

    /** Retroceder (Q): submenú → raíz; raíz → cerrar solo si terminó. */
    public static void atras(@Nonnull PlayerRef playerRef,
                             @Nonnull Ref<EntityStore> ref,
                             @Nonnull Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null || !d.menuAbierto) return;
        if (d.menuNivel == 1) {
            d.menuNivel = 0;
            d.menuSub = "";
            d.menuIdx = 0;
            DigiBattleHud.actualizar(playerRef, ref, store);
            return;
        }
        EstadoCombate c = d.combate;
        if (c != null && c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            cerrar(playerRef, ref, store);
        } else {
            if (c != null) c.logear("Usa HUIR para escapar del combate.");
            DigiBattleHud.actualizar(playerRef, ref, store);
        }
    }

    private static void trasAccion(PlayerRef playerRef, Ref<EntityStore> ref,
                                   Store<EntityStore> store, DatosJugador d, EstadoCombate c) {
        SistemaPaseo.prepararBatalla(playerRef, ref, store);
        DigiBattleHud.actualizar(playerRef, ref, store);
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            guardarFin(ref, store, d);
            if (c.victoria) {
                registrarVictoria(d, c, playerRef, ref, store);
            }
        }
    }

    /** Contador de victorias por especie: cada 5 vs la misma, Dato Incubación de su bebé. */
    private static final java.util.Map<java.util.UUID, java.util.Map<String, Integer>> VICTORIAS =
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final int VICTORIAS_PARA_DATO = 5;

    private static void registrarVictoria(DatosJugador d, EstadoCombate c, PlayerRef playerRef,
                                          Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            String bebe = SistemaIncubacion.bebeDe(c.enemigoNombre);
            if (bebe == null) return;
            java.util.Map<String, Integer> mapa =
                VICTORIAS.computeIfAbsent(playerRef.getUuid(), k -> new java.util.HashMap<>());
            int n = mapa.getOrDefault(bebe, 0) + 1;
            mapa.put(bebe, n);
            if (n % VICTORIAS_PARA_DATO != 0) return;
            String itemId = "Dato_Incubacion_" + bebe;
            com.hypixel.hytale.server.core.inventory.ItemStack stack =
                new com.hypixel.hytale.server.core.inventory.ItemStack(itemId, 1);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            Player.giveItem(stack, ref, store);
            playerRef.sendMessage(com.digitale.util.DigiMsg.parse(
                "§d> ¡Dato Incubacion de " + bebe + "! (" + n + " victorias) Examinalo con clic derecho."));
            LOGGER.log(Level.INFO, "DatoIncubacion " + bebe + " para " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "registrarVictoria: " + e.getMessage());
        }
    }

    private static void guardarFin(Ref<EntityStore> ref, Store<EntityStore> store, DatosJugador d) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(d);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar fin de combate: " + e.getMessage());
        }
    }

    private static EstadoCombate.Tactica ciclar(EstadoCombate.Tactica t) {
        return switch (t) {
            case AGRESIVO -> EstadoCombate.Tactica.EQUILIBRADO;
            case EQUILIBRADO -> EstadoCombate.Tactica.DEFENSIVO;
            case DEFENSIVO -> EstadoCombate.Tactica.AGRESIVO;
        };
    }

    public static String titulo(DatosJugador d) {
        if (d.menuNivel == 1) {
            return switch (d.menuSub) {
                case "combate" -> "MOVIMIENTOS";
                case "equipo" -> "EQUIPO";
                case "tactica" -> "TACTICAS";
                case "inventario" -> "MOCHILA";
                default -> "COMBATE";
            };
        }
        return "COMBATE";
    }

    public static List<String> listaItems(DatosJugador d) {
        List<String> out = new ArrayList<>();
        if (d.menuNivel == 0) {
            out.add("Combate");
            out.add("Equipo");
            out.add("Tactica");
            out.add("Inventario");
            out.add("Huir");
            return out;
        }
        EstadoCombate c = d.combate;
        switch (d.menuSub) {
            case "combate" -> {
                DatoDigimon lider = liderDe(d);
                java.util.List<DatoDigimon.Movimiento> movs =
                    lider != null ? lider.movimientosEquipados() : java.util.Collections.emptyList();
                for (DatoDigimon.Movimiento m : movs) {
                    out.add(m.nombre + " [" + m.coste + "]");
                }
                out.add("Pasar");
            }
            case "equipo" -> {
                java.util.List<DatoDigimon> party =
                    (c != null && c.party != null) ? c.party : java.util.Collections.emptyList();
                for (int i = 0; i < DatosJugador.MAX_EQUIPO; i++) {
                    DatoDigimon digi = d.getSlot(i);
                    if (digi == null) {
                        out.add((i + 1) + ". ---");
                    } else {
                        String marca = "";
                        if (party.contains(digi)) marca += " [CAMPO]";
                        else if (digi.dormido) marca += " [Zzz]";
                        else if (!digi.vivo || digi.hp <= 0) marca += " [KO]";
                        out.add((i + 1) + ". " + digi.nombre + " " + digi.hp + "/" + digi.maxHp + marca);
                    }
                }
            }
            case "tactica" -> {
                EstadoCombate.Tactica ta = c != null ? c.tacticaA : EstadoCombate.Tactica.EQUILIBRADO;
                EstadoCombate.Tactica tb = c != null ? c.tacticaB : EstadoCombate.Tactica.EQUILIBRADO;
                EstadoCombate.Tactica tc = c != null ? c.tacticaC : EstadoCombate.Tactica.EQUILIBRADO;
                out.add("A: " + ta);
                out.add("B: " + tb);
                out.add("C: " + tc);
            }
            case "inventario" -> {
                out.add("(Objetos de combate proximamente)");
            }
            default -> { }
        }
        return out;
    }
}
