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
import com.hypixel.hytale.protocol.packets.interface_.Page;
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
        // Cerrar la Page clickeable (si sigue abierta). menuAbierto ya es false,
        // asi que onDismiss no vuelve a entrar en cerrar().
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().setPage(ref, store, Page.None);
            }
        } catch (Exception ignored) {
        }
        SistemaPaseo.recogerBatalla(playerRef, ref, store);
        DigiBattleHud.ocultar(playerRef, ref, store);
        com.digitale.sistema.CamaraCombate.salir(playerRef);
        com.digitale.sistema.EscenarioBatalla.detener(playerRef, ref, store);
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
                case 0 -> {
                    atacarBasico(playerRef, ref, store, d, c);
                    return;
                }
                case 1 -> {
                    d.menuNivel = 1;
                    d.menuSub = "habilidad";
                    d.menuIdx = 0;
                }
                case 2 -> {
                    // APOYO: carga OP y pasa el turno (equivalente al Guard/Charge).
                    SistemaBatalla.support(c);
                    trasAccion(playerRef, ref, store, d, c);
                    return;
                }
                case 3 -> {
                    d.menuNivel = 1;
                    d.menuSub = "cambio";
                    d.menuIdx = 0;
                }
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
            case "habilidad" -> {
                DatoDigimon lider = liderDe(d);
                java.util.List<DatoDigimon.Movimiento> skills = habilidadesDe(lider);
                if (d.menuIdx < skills.size()) {
                    SistemaBatalla.ejecutarMovimiento(c, lider, skills.get(d.menuIdx));
                } else {
                    SistemaBatalla.pasarTurno(c);
                }
                trasAccion(playerRef, ref, store, d, c);
            }
            case "cambio" -> {
                java.util.List<Integer> slots = slotsCambio(d);
                if (d.menuIdx < slots.size()) {
                    if (!SistemaBatalla.intercambiarLider(c, d, slots.get(d.menuIdx))) {
                        c.logear("No puedes sacar a ese (dormido, KO o ya en campo).");
                        DigiBattleHud.actualizar(playerRef, ref, store);
                    } else {
                        SistemaBatalla.pasarTurno(c);
                        trasAccion(playerRef, ref, store, d, c);
                    }
                } else {
                    DigiBattleHud.actualizar(playerRef, ref, store);
                }
            }
            default -> DigiBattleHud.actualizar(playerRef, ref, store);
        }
    }

    /** ATACAR: usa el movimiento basico (primero equipado). */
    private static void atacarBasico(PlayerRef playerRef, Ref<EntityStore> ref,
                                     Store<EntityStore> store, DatosJugador d, EstadoCombate c) {
        DatoDigimon lider = liderDe(d);
        java.util.List<DatoDigimon.Movimiento> movs =
            lider != null ? lider.movimientosEquipados() : java.util.Collections.emptyList();
        if (!movs.isEmpty()) {
            SistemaBatalla.ejecutarMovimiento(c, lider, movs.get(0));
        } else {
            SistemaBatalla.pasarTurno(c);
        }
        trasAccion(playerRef, ref, store, d, c);
    }

    /** HABILIDAD: movimientos equipados excepto el ataque basico. */
    private static java.util.List<DatoDigimon.Movimiento> habilidadesDe(DatoDigimon lider) {
        if (lider == null) return java.util.Collections.emptyList();
        java.util.List<DatoDigimon.Movimiento> movs = lider.movimientosEquipados();
        if (movs.size() <= 1) return java.util.Collections.emptyList();
        return new ArrayList<>(movs.subList(1, movs.size()));
    }

    /** CAMBIAR: solo reservas disponibles (fuera de campo, vivas y despiertas). */
    private static java.util.List<Integer> slotsCambio(DatosJugador d) {
        java.util.List<Integer> out = new ArrayList<>();
        EstadoCombate c = d.combate;
        java.util.List<DatoDigimon> party = (c != null && c.party != null && !c.party.isEmpty())
            ? c.party : d.equipoCombate();
        for (int i = 0; i < DatosJugador.MAX_EQUIPO; i++) {
            DatoDigimon digi = d.getSlot(i);
            if (digi == null) continue;
            if (party.contains(digi)) continue;
            if (digi.dormido || !digi.vivo || digi.hp <= 0) continue;
            out.add(i);
        }
        return out;
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
            if (c != null) c.logear("Usa RETIRARSE para escapar del combate.");
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
            // Cada 2 victorias: esencia (moneda). Cada 5: dato incubación.
            if (n % 2 == 0) {
                darItem(playerRef, ref, store, "Esencia_" + bebe,
                    "§b> Esencia de " + bebe + " (" + n + " victorias).");
            }
            if (n % VICTORIAS_PARA_DATO != 0) return;
            String itemId = "Dato_Incubacion_" + bebe;
            darItem(playerRef, ref, store, itemId,
                "§d> ¡Dato Incubacion de " + bebe + "! (" + n + " victorias) Examinalo con clic derecho.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "registrarVictoria: " + e.getMessage());
        }
    }

    private static void darItem(PlayerRef playerRef, Ref<EntityStore> ref,
                                Store<EntityStore> store, String itemId, String mensaje) {
        try {
            com.hypixel.hytale.server.core.inventory.ItemStack stack =
                new com.hypixel.hytale.server.core.inventory.ItemStack(itemId, 1);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            Player.giveItem(stack, ref, store);
            playerRef.sendMessage(com.digitale.util.DigiMsg.parse(mensaje));
            LOGGER.log(Level.INFO, itemId + " para " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "darItem " + itemId + ": " + e.getMessage());
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
                case "habilidad" -> "HABILIDAD";
                case "cambio" -> "CAMBIAR";
                default -> "COMBATE";
            };
        }
        return "COMBATE";
    }

    public static List<String> listaItems(DatosJugador d) {
        List<String> out = new ArrayList<>();
        if (d.menuNivel == 0) {
            out.add("ATACAR");
            out.add("HABILIDAD");
            out.add("APOYO");
            out.add("CAMBIAR");
            out.add("HUIR");
            return out;
        }
        EstadoCombate c = d.combate;
        switch (d.menuSub) {
            case "habilidad" -> {
                DatoDigimon lider = liderDe(d);
                for (DatoDigimon.Movimiento m : habilidadesDe(lider)) {
                    out.add(m.nombre + " [" + m.coste + "]");
                }
                out.add("Pasar");
            }
            case "cambio" -> {
                java.util.List<Integer> slots = slotsCambio(d);
                if (slots.isEmpty()) {
                    out.add("Sin reservas disponibles");
                } else {
                    for (int slot : slots) {
                        DatoDigimon digi = d.getSlot(slot);
                        if (digi == null) continue;
                        out.add((slot + 1) + ". " + digi.nombre + " " + digi.hp + "/" + digi.maxHp);
                    }
                }
            }
            default -> { }
        }
        return out;
    }
}
