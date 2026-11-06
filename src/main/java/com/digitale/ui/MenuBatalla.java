package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.TablaTipos;
import com.digitale.datos.EstadoCombate.Enemigo;
import com.digitale.sistema.CamaraCombate;
import com.digitale.sistema.MusicaCombate;
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
 *
 * <p>Estructura de menús:</p>
 * <pre>
 *   ATACAR            → usa el primer movimiento equipado
 *   HABILIDAD         → movimientos con su coste de OP
 *   APOYO             → PROTEGER / CONCENTRAR / ANALIZAR / ARTE CRUZADO
 *   CAMBIAR           → saca una reserva (NO consume turno)
 *   HUIR              → intenta escapar
 * </pre>
 *
 * <p>Solo se puede actuar cuando {@link EstadoCombate#esperandoJugador} es
 * true. Todo aquí corre en el hilo del mundo.</p>
 */
public final class MenuBatalla {

    private MenuBatalla() {
    }

    private static final Logger LOGGER = Logger.getLogger(MenuBatalla.class.getName());

    /**
     * Ataque básico: gratis y siempre disponible (ATACAR nunca se queda sin OP).
     * El daño real de las habilidades viene de {@code movimientosEquipados()}.
     */
    public static final DatoDigimon.Movimiento ATAQUE_BASICO =
        new DatoDigimon.Movimiento("Ataque básico", "FISICO", 1.1, null, 0, 0, 1);

    // ══ Entrada / salida del combate ═══════════════════════════════════

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
        MusicaCombate.iniciar(playerRef);
        DigiBattleHud.mostrar(playerRef, ref, store);
        DigiBattleHud.actualizar(playerRef, ref, store);
    }

    /** Cerrar menú (Q en raíz tras FIN, huida exitosa). Recoge formación. */
    public static void cerrar(@Nonnull PlayerRef playerRef,
                              @Nonnull Ref<EntityStore> ref,
                              @Nonnull Store<EntityStore> store) {
        MusicaCombate.detener(playerRef);
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d != null) {
            d.menuAbierto = false;
            d.menuNivel = 0;
            d.menuIdx = 0;
            d.menuSub = "";
            d.menuMovimiento = null;
            d.menuSubAnterior = "";
            // Por si el jugador cierra justo al terminar: no perder recompensas.
            notificarFin(playerRef, ref, store);
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
        CamaraCombate.salir(playerRef);
        com.digitale.sistema.EscenarioBatalla.detener(playerRef, ref, store);
    }

    // ══ Navegación ═════════════════════════════════════════════════════

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

    // ══ Aceptar ════════════════════════════════════════════════════════

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

        // Combate terminado: cualquier opción cierra.
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            cerrar(playerRef, ref, store);
            return;
        }

        List<String> items = listaItems(d);
        if (items.isEmpty()) return;
        if (d.menuIdx < 0 || d.menuIdx >= items.size()) return;

        if (d.menuNivel == 0) {
            if (!c.esperandoJugador) {
                c.logear("Espera tu turno...");
                DigiBattleHud.actualizar(playerRef, ref, store);
                return;
            }
            switch (d.menuIdx) {
                case 0 -> {
                    d.menuMovimiento = ATAQUE_BASICO;
                    d.menuSubAnterior = "";
                    entrarSubmenu(d, "objetivoAtaque");
                }
                case 1 -> entrarSubmenu(d, "habilidad");
                case 2 -> entrarSubmenu(d, "apoyo");
                case 3 -> entrarSubmenu(d, "cambio");
                case 4 -> {
                    if (SistemaBatalla.huir(c)) {
                        guardarFin(ref, store, d);
                        cerrar(playerRef, ref, store);
                        return;
                    }
                    trasAccion(playerRef, ref, store, d, c);
                    return;
                }
                default -> { }
            }
            DigiBattleHud.actualizar(playerRef, ref, store);
            return;
        }

        switch (d.menuSub) {
            case "habilidad" -> {
                DatoDigimon lider = liderDe(d);
                List<DatoDigimon.Movimiento> skills = habilidadesDe(lider);
                if (d.menuIdx < skills.size()) {
                    // Primero la habilidad, DESPUÉS el objetivo.
                    d.menuMovimiento = skills.get(d.menuIdx);
                    d.menuSubAnterior = "habilidad";
                    entrarSubmenu(d, "objetivoAtaque");
                    DigiBattleHud.actualizar(playerRef, ref, store);
                    return;
                }
                SistemaBatalla.pasarTurno(c);
                trasAccion(playerRef, ref, store, d, c);
            }
            case "apoyo" -> {
                switch (d.menuIdx) {
                    case 0 -> SistemaBatalla.guardar(c);
                    case 1 -> SistemaBatalla.support(c);
                    case 2 -> {
                        // Analizar NO consume turno: se queda en el menú.
                        SistemaBatalla.analizar(c);
                        d.menuNivel = 0;
                        d.menuSub = "";
                        d.menuIdx = 0;
                        DigiBattleHud.actualizar(playerRef, ref, store);
                        return;
                    }
                    case 3 -> {
                        if (!SistemaBatalla.arteCruzado(c)) {
                            DigiBattleHud.actualizar(playerRef, ref, store);
                            return;
                        }
                        // El Arte Cruzado tampoco consume turno.
                        d.menuNivel = 0;
                        d.menuSub = "";
                        d.menuIdx = 0;
                        DigiBattleHud.actualizar(playerRef, ref, store);
                        return;
                    }
                    default -> { }
                }
                trasAccion(playerRef, ref, store, d, c);
            }
            case "objetivoAtaque" -> {
                List<Integer> vivos = enemigosVivos(c);
                if (d.menuIdx < vivos.size() && d.menuMovimiento != null) {
                    DatoDigimon activo = liderDe(d);
                    DatoDigimon.Movimiento mov = d.menuMovimiento;
                    d.menuMovimiento = null;
                    d.menuSubAnterior = "";
                    if (SistemaBatalla.ejecutarMovimiento(c, activo, mov, vivos.get(d.menuIdx))) {
                        trasAccion(playerRef, ref, store, d, c);
                        return;
                    }
                }
                d.menuNivel = 0;
                d.menuSub = "";
                d.menuIdx = 0;
                d.menuMovimiento = null;
                DigiBattleHud.actualizar(playerRef, ref, store);
            }
            case "cambio" -> {
                List<Integer> slots = slotsCambio(d);
                if (d.menuIdx < slots.size()) {
                    // Cambiar NO consume turno: seguimos en el mismo turno.
                    if (SistemaBatalla.cambiarActivo(c, d, slots.get(d.menuIdx))) {
                        d.menuNivel = 0;
                        d.menuSub = "";
                        d.menuIdx = 0;
                        SistemaPaseo.prepararBatalla(playerRef, ref, store);
                    }
                }
                DigiBattleHud.actualizar(playerRef, ref, store);
            }
            default -> DigiBattleHud.actualizar(playerRef, ref, store);
        }
    }

    private static void entrarSubmenu(DatosJugador d, String sub) {
        d.menuNivel = 1;
        d.menuSub = sub;
        d.menuIdx = 0;
    }

    /** HABILIDAD: movimientos equipados excepto el ataque básico. */
    private static List<DatoDigimon.Movimiento> habilidadesDe(DatoDigimon lider) {
        if (lider == null) return java.util.Collections.emptyList();
        List<DatoDigimon.Movimiento> movs = lider.movimientosEquipados();
        if (movs.size() <= 1) return java.util.Collections.emptyList();
        return new ArrayList<>(movs.subList(1, movs.size()));
    }

    /** CAMBIAR: solo reservas disponibles (fuera de campo, vivas y despiertas). */
    private static List<Integer> slotsCambio(DatosJugador d) {
        List<Integer> out = new ArrayList<>();
        EstadoCombate c = d.combate;
        List<DatoDigimon> party = (c != null && c.party != null && !c.party.isEmpty())
            ? c.party : d.equipoCombate();
        for (int i = 0; i < DatosJugador.MAX_EQUIPO; i++) {
            DatoDigimon digi = d.getSlot(i);
            if (digi == null) continue;
            if (party.contains(digi)) continue;
            if (digi.dormido || !digi.vivo || digi.hp <= 0 || digi.noCombate) continue;
            out.add(i);
        }
        return out;
    }

    /** Digimon que tiene el turno (cualquiera de los tres; el jugador decide). */
    private static DatoDigimon liderDe(DatosJugador d) {
        EstadoCombate c = d.combate;
        if (c != null && c.actorActual != null && c.actorActual.aliado()) {
            DatoDigimon activo = c.digimonDe(c.actorActual.idx());
            if (activo != null) return activo;
        }
        if (c != null && c.party != null && !c.party.isEmpty()) return c.party.get(0);
        return d.getA();
    }

    /** Retroceder (Q): submenú → raíz; raíz → cerrar solo si terminó. */
    public static void atras(@Nonnull PlayerRef playerRef,
                             @Nonnull Ref<EntityStore> ref,
                             @Nonnull Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null || !d.menuAbierto) return;
        if (d.menuNivel == 1) {
            if ("objetivoAtaque".equals(d.menuSub) && "habilidad".equals(d.menuSubAnterior)) {
                d.menuSub = "habilidad";
                d.menuNivel = 1;
            } else {
                d.menuNivel = 0;
                d.menuSub = "";
            }
            d.menuIdx = 0;
            d.menuMovimiento = null;
            d.menuSubAnterior = "";
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
        d.menuNivel = 0;
        d.menuSub = "";
        d.menuIdx = 0;
        SistemaPaseo.prepararBatalla(playerRef, ref, store);
        // Si la acción generó una animación, la cámara la controla la escena:
        // mandarla al plano general aquí causaría un salto antes del foco.
        if (c.eventos.isEmpty()) CamaraCombate.actualizar(playerRef);
        DigiBattleHud.actualizar(playerRef, ref, store);
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            notificarFin(playerRef, ref, store);
        }
    }

    /** Aplica fin de combate (guardado + recompensas) una sola vez. */
    public static void notificarFin(PlayerRef playerRef, Ref<EntityStore> ref,
                                    Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null || d.combate == null) return;
        EstadoCombate c = d.combate;
        if (c.fase != EstadoCombate.Fase.FIN_COMBATE || c.recompensasDadas) return;
        c.recompensasDadas = true;
        guardarFin(ref, store, d);
        if (c.victoria) registrarVictoria(d, c, playerRef, ref, store);
        DigiBattleHud.actualizar(playerRef, ref, store);
    }

    // ══ Recompensas ════════════════════════════════════════════════════

    /** Contador de victorias por especie: cada 5 vs la misma, Dato Incubación. */
    private static final java.util.Map<java.util.UUID, java.util.Map<String, Integer>> VICTORIAS =
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final int VICTORIAS_PARA_DATO = 5;

    private static void registrarVictoria(DatosJugador d, EstadoCombate c, PlayerRef playerRef,
                                          Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            java.util.Set<String> especies = new java.util.LinkedHashSet<>();
            for (Enemigo e : c.enemigos) {
                if (e != null && e.nombre != null) especies.add(e.nombre);
            }
            java.util.Map<String, Integer> mapa =
                VICTORIAS.computeIfAbsent(playerRef.getUuid(), k -> new java.util.HashMap<>());
            for (String nombreEnem : especies) {
                String bebe = SistemaIncubacion.bebeDe(nombreEnem);
                if (bebe == null) continue;
                int n = mapa.getOrDefault(bebe, 0) + 1;
                mapa.put(bebe, n);
                // Cada 2 victorias: esencia (moneda). Cada 5: dato incubación.
                if (n % 2 == 0) {
                    darItem(playerRef, ref, store, "Esencia_" + bebe,
                        "§b> Esencia de " + bebe + " (" + n + " victorias).");
                }
                if (n % VICTORIAS_PARA_DATO != 0) continue;
                String itemId = "Dato_Incubacion_" + bebe;
                darItem(playerRef, ref, store, itemId,
                    "§d> ¡Dato Incubacion de " + bebe + "! (" + n + " victorias) Examinalo con clic derecho.");
            }
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

    // ══ Textos del menú ═════════════════════════════════════════════════

    public static String titulo(DatosJugador d) {
        if (d.menuNivel == 1) {
            return switch (d.menuSub) {
                case "habilidad" -> "HABILIDAD";
                case "apoyo" -> "APOYO";
                case "objetivoAtaque" -> "OBJETIVO";
                case "cambio" -> "CAMBIAR";
                default -> "COMBATE";
            };
        }
        return "COMBATE";
    }

    public static List<String> listaItems(DatosJugador d) {
        List<String> out = new ArrayList<>();
        EstadoCombate c = d.combate;

        if (c != null && c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            out.add(c.victoria ? "CERRAR (VICTORIA)" : "CERRAR (DERROTA)");
            return out;
        }

        if (d.menuNivel == 0) {
            // Exactamente 5 opciones: la UI clickeable tiene 5 botones raíz.
            out.add("ATACAR");
            out.add("HABILIDAD");
            out.add("APOYO");
            out.add("CAMBIAR");
            out.add("HUIR");
            return out;
        }

        switch (d.menuSub) {
            case "habilidad" -> {
                DatoDigimon lider = liderDe(d);
                int opActivo = (c != null && c.actorActual != null && c.actorActual.aliado())
                    ? c.opDe(c.actorActual.idx()) : 0;
                for (DatoDigimon.Movimiento m : habilidadesDe(lider)) {
                    boolean alcanza = c == null || opActivo >= m.coste;
                    out.add(m.nombre + " [" + m.coste + "]" + (alcanza ? "" : "  ✗"));
                }
                out.add("Pasar");
            }
            case "apoyo" -> {
                boolean listoCp = c != null && c.cpListoParaArte();
                out.add("PROTEGER (guardia)");
                out.add("CONCENTRAR (OP/CP)");
                out.add("ANALIZAR (gratis)");
                out.add("ARTE CRUZADO" + (listoCp ? "" : "  ✗ CP " + (c != null ? c.cp : 0)));
            }
            case "objetivoAtaque" -> {
                List<Integer> vivos = enemigosVivos(c);
                DatoDigimon activo = liderDe(d);
                if (vivos.isEmpty()) {
                    out.add("Sin enemigos vivos");
                } else {
                    for (int idx : vivos) {
                        Enemigo e = c.enemigo(idx);
                        if (e == null) continue;
                        double mult = activo != null
                            ? TablaTipos.multTotal(activo.elemento, activo.atributo, e.elemento, e.atributo)
                            : 1.0;
                        out.add((idx == c.objetivoIdx ? "▶ " : "   ") + e.nombre
                            + " " + e.hp + "/" + e.maxHp + " [" + e.elemento + "/"
                            + TablaTipos.textoAtributo(e.atributo) + "] "
                            + TablaTipos.estadoContra(mult));
                    }
                }
            }
            case "cambio" -> {
                List<Integer> slots = slotsCambio(d);
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
            default -> out.add("Volver");
        }
        return out;
    }

    /** Línea de estado que se muestra bajo el título del menú. */
    public static String estadoTurno(EstadoCombate c) {
        if (c == null) return "";
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            return (c.victoria ? "VICTORIA" : "DERROTA") + " · " + c.expTotal + " XP";
        }
        if (c.cinematicaActiva) {
            String actor = c.actorActual != null ? c.nombreDe(c.actorActual) : "";
            return "ANIMANDO" + (actor.isEmpty() ? "" : " · " + actor) + "...";
        }
        if (c.esperandoJugador && c.actorActual != null && c.actorActual.aliado()) {
            return "TU TURNO · " + c.nombreDe(c.actorActual)
                + " (" + letra(c.actorActual.idx()) + ")";
        }
        if (c.actorActual != null) {
            return "TURNO DE " + c.nombreDe(c.actorActual).toUpperCase();
        }
        return "RESOLVIENDO...";
    }

    /** Índices de enemigos vivos, en el orden del grupo. */
    private static java.util.List<Integer> enemigosVivos(EstadoCombate c) {
        java.util.List<Integer> out = new ArrayList<>();
        if (c == null) return out;
        for (int i = 0; i < c.enemigos.size(); i++) {
            com.digitale.datos.EstadoCombate.Enemigo e = c.enemigos.get(i);
            if (e != null && e.vivo()) out.add(i);
        }
        return out;
    }

    private static String letra(int idx) {
        return switch (idx) {
            case 0 -> "A";
            case 1 -> "B";
            default -> "C";
        };
    }

    /** Texto del objetivo actual para el HUD. */
    public static String objetivoTexto(EstadoCombate c) {
        if (c == null) return "";
        Enemigo e = c.objetivoActual();
        if (e == null) return "";
        DatoDigimon activo = c.actorActual != null && c.actorActual.aliado()
            ? c.digimonDe(c.actorActual.idx()) : c.digimonDe(0);
        double mult = TablaTipos.multTotal(
            activo != null ? activo.elemento : "NEUTRO",
            activo != null ? activo.atributo : "FREE",
            e.elemento, e.atributo);
        return e.nombre + " [" + e.elemento + "/" + TablaTipos.textoAtributo(e.atributo) + "] · "
            + TablaTipos.estadoContra(mult);
    }
}
