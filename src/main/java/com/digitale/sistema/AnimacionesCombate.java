package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.Actor;
import com.digitale.datos.EstadoCombate.Enemigo;
import com.digitale.datos.EstadoCombate.EventoVisual;
import com.digitale.ui.DigiBattleHud;
import com.digitale.ui.MenuBatalla;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Capa de presentación del combate: reproduce los {@link EventoVisual} que
 * registra {@link SistemaBatalla} con cámara cinematográfica y animaciones del
 * modelo, y es la que avanza el combate (un actor por vez) cuando la animación
 * anterior ha terminado.
 *
 * <p>Solo usa primitivas de animación que el propio motor NPC utiliza
 * ({@link NPCEntity#playAnimation}); no fuerza estados ni toca anclas de UI.</p>
 */
public final class AnimacionesCombate {

    private AnimacionesCombate() {
    }

    private static final Logger LOGGER = Logger.getLogger(AnimacionesCombate.class.getName());

    private static final long TICK_MS = 90L;
    private static final long FASE_ATAQUE_MS = 650L;
    private static final long FASE_DANO_MS = 900L;
    private static final long FASE_MUERTE_MS = 1500L;
    private static final long PAUSA_ENTRE_EVENTOS_MS = 260L;
    /** Tiempo para que la cámara llegue al atacante antes de su animación. */
    private static final long FASE_LLEGADA_ATAQUE_MS = 240L;
    /** Tiempo para que la cámara llegue al objetivo antes del Hurt/Death. */
    private static final long FASE_LLEGADA_DANO_MS = 280L;
    /** Milisegundos de plano general antes de empezar el foco (preparación). */
    private static final long PRE_ROLL_MS = 300L;
    // Turnos enemigos: sin plano de cámara y más cortos (ágiles y legibles).
    private static final long FASE_ATAQUE_RAPIDA_MS = 450L;
    private static final long FASE_DANO_RAPIDA_MS = 700L;
    private static final long FASE_MUERTE_RAPIDA_MS = 1300L;
    private static final long IDLE_CADA_MS = 650L;
    /** Enfocar también los golpes enemigos (false = quedarse en plano general). */
    private static final boolean ENFOCAR_ENEMIGOS = true;
    private static final long DESPAWN_MUERTO_MS = 1800L;

    private static final ScheduledExecutorService SCHED =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "digitale-anim-combate");
            t.setDaemon(true);
            return t;
        });

    private static final Map<UUID, Sesion> SESIONES = new ConcurrentHashMap<>();

    private static final class Sesion {
        Ref<EntityStore> ref;
        ScheduledFuture<?> task;
        EventoVisual evento;
        int fase;              // 0 = nada, 1 = atacante, 2 = objetivo
        long faseDesde;
        long esperaHasta;
        long ultimoIdle;
        boolean enfocado;
        boolean primerIdle;
    }

    // ══════════════════════════════════════════════════════════════════
    // API
    // ══════════════════════════════════════════════════════════════════

    public static void iniciar(PlayerRef playerRef, Ref<EntityStore> ref,
                               Store<EntityStore> store) {
        if (playerRef == null || ref == null) return;
        detener(playerRef);
        Sesion s = new Sesion();
        s.ref = ref;
        s.ultimoIdle = System.currentTimeMillis() + 1500L;
        s.task = SCHED.scheduleAtFixedRate(() -> {
            try {
                if (!ref.isValid()) {
                    detener(playerRef);
                    return;
                }
                Store<EntityStore> st = ref.getStore();
                World world = st.getExternalData().getWorld();
                if (world == null) {
                    detener(playerRef);
                    return;
                }
                world.execute(() -> {
                    try {
                        tick(playerRef, ref, st);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "AnimacionesCombate tick: " + ex.getMessage());
                        AlmacenJugadores.DatosJugador dd =
                            AlmacenJugadores.getDatos(playerRef.getUuid());
                        if (dd != null && dd.combate != null) {
                            dd.combate.cinematicaActiva = false;
                            dd.combate.eventos.clear();
                        }
                        detener(playerRef);
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "AnimacionesCombate task: " + e.getMessage());
            }
        }, TICK_MS, TICK_MS, TimeUnit.MILLISECONDS);
        SESIONES.put(playerRef.getUuid(), s);
        LOGGER.log(Level.INFO, "AnimacionesCombate iniciado: " + playerRef.getUsername());
    }

    /** ¿La presentación sigue viva? (fallback de la escena). */
    public static boolean activa(UUID uuid) {
        Sesion s = SESIONES.get(uuid);
        return s != null && s.task != null && !s.task.isCancelled() && !s.task.isDone();
    }

    public static void detener(PlayerRef playerRef) {
        if (playerRef == null) return;
        detener(playerRef.getUuid());
    }

    public static void detener(UUID uuid) {
        Sesion s = SESIONES.remove(uuid);
        if (s == null) return;
        if (s.task != null) s.task.cancel(false);
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(uuid);
        if (d != null && d.combate != null) {
            d.combate.cinematicaActiva = false;
            d.combate.eventos.clear();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // BUCLE
    // ══════════════════════════════════════════════════════════════════

    private static void tick(PlayerRef playerRef, Ref<EntityStore> ref,
                             Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        EstadoCombate c = d != null ? d.combate : null;
        Sesion s = SESIONES.get(playerRef.getUuid());
        if (d == null || !d.menuAbierto || c == null || s == null) {
            detener(playerRef);
            return;
        }

        long now = System.currentTimeMillis();

        if (!c.activa && c.eventos.isEmpty() && s.evento == null) {
            if (s.enfocado) volverAlPlanoGeneral(playerRef, c, s);
            c.cinematicaActiva = false;
            return;
        }

        if (s.evento == null && now >= s.esperaHasta && !c.eventos.isEmpty()) {
            s.evento = c.eventos.remove(0);
            s.fase = 0;               // pre-roll: plano general unos ms
            s.faseDesde = now;
            c.cinematicaActiva = true;
            return;
        }

        if (s.evento != null) {
            long dt = now - s.faseDesde;
            boolean mio = s.evento.atacanteAliado;
            long faseAtaque = mio ? FASE_ATAQUE_MS : FASE_ATAQUE_RAPIDA_MS;
            long faseObjetivo = s.evento.muerte
                ? (mio ? FASE_MUERTE_MS : FASE_MUERTE_RAPIDA_MS)
                : (mio ? FASE_DANO_MS : FASE_DANO_RAPIDA_MS);
            switch (s.fase) {
                case 0 -> { // pre-roll en plano general
                    if (dt >= PRE_ROLL_MS) {
                        s.fase = 1;
                        s.faseDesde = now;
                        empezarAtaque(playerRef, d, c, s, s.evento);
                    }
                }
                case 1 -> { // la cámara viaja al atacante
                    if (dt >= FASE_LLEGADA_ATAQUE_MS) {
                        s.fase = 2;
                        s.faseDesde = now;
                        reproducirAtaque(playerRef, d, c, s, s.evento);
                    }
                }
                case 2 -> { // animación de ataque
                    if (dt >= faseAtaque) {
                        s.fase = 3;
                        s.faseDesde = now;
                        enfocarObjetivo(playerRef, d, c, s, s.evento);
                    }
                }
                case 3 -> { // la cámara viaja al objetivo
                    if (dt >= FASE_LLEGADA_DANO_MS) {
                        s.fase = 4;
                        s.faseDesde = now;
                        reproducirDano(playerRef, d, c, s, s.evento);
                    }
                }
                case 4 -> { // animación de daño/muerte
                    if (dt >= faseObjetivo) {
                        s.evento = null;
                        s.fase = 0;
                        s.esperaHasta = now + PAUSA_ENTRE_EVENTOS_MS;
                        if (c.eventos.isEmpty()) volverAlPlanoGeneral(playerRef, c, s);
                    }
                }
                default -> { }
            }
            return;
        }

        // Sin eventos: la escena avanza el combate (un actor) y mantiene Idle.
        if (s.enfocado) volverAlPlanoGeneral(playerRef, c, s);
        c.cinematicaActiva = false;

        if (c.activa && now >= s.esperaHasta) {
            SistemaBatalla.procesarTurnos(c);
            if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
                MenuBatalla.notificarFin(playerRef, s.ref, s.ref.getStore());
                return;
            }
        }

        if (c.activa && now - s.ultimoIdle >= IDLE_CADA_MS) {
            s.ultimoIdle = now;
            if (!s.primerIdle) {
                s.primerIdle = true;
                LOGGER.log(Level.INFO, "AnimacionesCombate primer idle");
            }
            reafirmarIdle(d, c, store);
        }
    }

    private static void empezarAtaque(PlayerRef playerRef, AlmacenJugadores.DatosJugador d,
                                      EstadoCombate c, Sesion s, EventoVisual e) {
        Ref<?> atacante = refDe(d, c, e.atacanteAliado, e.atacanteIdx);
        if (atacante == null || !atacante.isValid()) return;
        Ref<?> objetivo = refDe(d, c, e.objetivoAliado, e.objetivoIdx);
        if (objetivo != null) mirarA(s.ref.getStore(), atacante, objetivo);
        // Solo las acciones del equipo del jugador tienen plano cinematográfico:
        // los turnos enemigos se ven en plano general (menos cortes y más legible).
        if (e.atacanteAliado || ENFOCAR_ENEMIGOS) {
            CamaraCombate.enfocar(playerRef, atacante, e.atacanteAliado);
            s.enfocado = true;
        } else if (s.enfocado) {
            volverCamaraAncha(playerRef, s);
        }
    }

    /** Animación de ataque + aura nativa en el atacante (acciones del jugador). */
    private static void reproducirAtaque(PlayerRef playerRef, AlmacenJugadores.DatosJugador d,
                                         EstadoCombate c, Sesion s, EventoVisual e) {
        Ref<?> atacante = refDe(d, c, e.atacanteAliado, e.atacanteIdx);
        if (atacante == null || !atacante.isValid()) return;
        Store<EntityStore> store = s.ref.getStore();
        if (e.atacanteAliado) {
            Vector3d pos = posicionDe(store, atacante);
            if (pos != null) {
                pos.y += Math.max(0.5, c.alturaMedia * 0.5);
                ParticleUtil.spawnParticleEffect("SyncAura", pos, store);
            }
        }
        animar(store, atacante, AnimationSlot.Action,
            "Attack", "Shoot", "Interaction");
    }

    private static void enfocarObjetivo(PlayerRef playerRef, AlmacenJugadores.DatosJugador d,
                                        EstadoCombate c, Sesion s, EventoVisual e) {
        Ref<?> objetivo = refDe(d, c, e.objetivoAliado, e.objetivoIdx);
        if (objetivo == null || !objetivo.isValid()) return;

        Store<EntityStore> store = s.ref.getStore();
        Ref<?> atacante = refDe(d, c, e.atacanteAliado, e.atacanteIdx);
        if (atacante != null) mirarA(store, objetivo, atacante);
        if (e.atacanteAliado || ENFOCAR_ENEMIGOS) {
            CamaraCombate.enfocar(playerRef, objetivo, e.objetivoAliado);
            s.enfocado = true;
        }
    }

    /** Hurt/Death + partículas de impacto en el objetivo. */
    private static void reproducirDano(PlayerRef playerRef, AlmacenJugadores.DatosJugador d,
                                       EstadoCombate c, Sesion s, EventoVisual e) {
        Ref<?> objetivo = refDe(d, c, e.objetivoAliado, e.objetivoIdx);
        if (objetivo == null || !objetivo.isValid()) return;
        Store<EntityStore> store = s.ref.getStore();

        if (!e.objetivoAliado) {
            Enemigo en = c.enemigo(e.objetivoIdx);
            if (en != null && !en.vivo()) {
                en.despawnEn = System.currentTimeMillis() + DESPAWN_MUERTO_MS;
            }
        }

        Vector3d pos = posicionDe(store, objetivo);
        if (pos != null) {
            pos.y += Math.max(0.4, c.alturaMedia * 0.5);
            String elemento = elementoDe(c, e.atacanteAliado, e.atacanteIdx);
            String sistema = sistemaImpacto(elemento);
            if (sistema != null) ParticleUtil.spawnParticleEffect(sistema, pos, store);
            if (e.muerte) {
                ParticleUtil.spawnParticleEffect("Impact_Critical", pos, store);
            }
        }

        if (e.muerte) {
            animar(store, objetivo, AnimationSlot.Action, "Death", "Die", "Dead", "Hurt");
        } else if (e.dano > 0) {
            animar(store, objetivo, AnimationSlot.Action, "Hurt", "Hit", "Damage", "Surprised");
        }
    }

    private static Vector3d posicionDe(Store<EntityStore> store, Ref<?> ref) {
        if (ref == null || !ref.isValid()) return null;
        @SuppressWarnings("unchecked")
        TransformComponent t = store.getComponent((Ref<EntityStore>) ref,
            TransformComponent.getComponentType());
        return t != null ? new Vector3d(t.getPosition()) : null;
    }

    private static String elementoDe(EstadoCombate c, boolean aliado, int idx) {
        if (aliado) {
            DatoDigimon d = c.digimonDe(idx);
            return d != null && d.elemento != null ? d.elemento : "NEUTRO";
        }
        Enemigo e = c.enemigo(idx);
        return e != null && e.elemento != null ? e.elemento : "NEUTRO";
    }

    /** Sistema de partículas nativo para el impacto según el elemento. */
    private static String sistemaImpacto(String elemento) {
        if (elemento == null) return "Impact_Sword_Basic";
        return switch (elemento.toUpperCase(java.util.Locale.ROOT)) {
            case "FUEGO" -> "Impact_Fire";
            case "HIELO", "AGUA" -> "Impact_Ice";
            case "OSCURIDAD" -> "VoidImpact";
            case "NATURALEZA", "VENENO" -> "Impact_Poison";
            case "TRUENO", "LUZ", "VIENTO" -> "Impact_Critical";
            default -> "Impact_Sword_Basic";
        };
    }

    /** Gira el modelo para que mire al rival (solo yaw). */
    private static void mirarA(Store<EntityStore> store, Ref<?> quien, Ref<?> aQuien) {
        if (quien == null || aQuien == null || !quien.isValid() || !aQuien.isValid()) return;
        try {
            @SuppressWarnings("unchecked")
            TransformComponent t = store.getComponent((Ref<EntityStore>) quien,
                TransformComponent.getComponentType());
            @SuppressWarnings("unchecked")
            TransformComponent o = store.getComponent((Ref<EntityStore>) aQuien,
                TransformComponent.getComponentType());
            if (t == null || o == null) return;
            Vector3d p = t.getPosition(), q = o.getPosition();
            Vector3d dir = new Vector3d(q.x - p.x, 0, q.z - p.z);
            if (dir.lengthSquared() < 1.0e-4) return;
            Rotation3f look = Rotation3f.lookAt(dir);
            if (look != null) t.setRotation(new Rotation3f(0f, look.yaw(), 0f));
        } catch (Exception ignored) {
        }
    }

    private static void volverAlPlanoGeneral(PlayerRef playerRef, EstadoCombate c, Sesion s) {
        s.enfocado = false;
        c.cinematicaActiva = false;
        CamaraCombate.actualizar(playerRef);
    }

    private static void volverCamaraAncha(PlayerRef playerRef, Sesion s) {
        s.enfocado = false;
        CamaraCombate.actualizar(playerRef);
    }

    // ══════════════════════════════════════════════════════════════════
    // ANIMACIONES (solo la API estándar del motor NPC)
    // ══════════════════════════════════════════════════════════════════

    private static void reafirmarIdle(AlmacenJugadores.DatosJugador d, EstadoCombate c,
                                      Store<EntityStore> store) {
        for (int i = 0; i < 3; i++) {
            if (!c.vivo(Actor.aliado(i))) continue;
            Ref<?> r = (d.refsBatalla != null && i < d.refsBatalla.length) ? d.refsBatalla[i] : null;
            animarIdle(store, r);
        }
        for (Enemigo e : c.enemigos) {
            if (e == null || !e.vivo()) continue;
            animarIdle(store, e.ref);
        }
    }

    private static void animarIdle(Store<EntityStore> store, Ref<?> ref) {
        if (ref == null || !ref.isValid()) return;
        @SuppressWarnings("unchecked")
        Ref<EntityStore> r = (Ref<EntityStore>) ref;
        ActiveAnimationComponent act = store.getComponent(r,
            ActiveAnimationComponent.getComponentType());
        if (act != null) {
            String[] arr = act.getActiveAnimations();
            int idx = AnimationSlot.Status.ordinal();
            if (arr != null && idx < arr.length && "Idle".equals(arr[idx])) return;
        }
        animar(store, ref, AnimationSlot.Status, "Idle");
    }

    private static void animar(Store<EntityStore> store, Ref<?> ref,
                               AnimationSlot slot, String... candidatos) {
        if (ref == null || !ref.isValid()) return;
        @SuppressWarnings("unchecked")
        Ref<EntityStore> r = (Ref<EntityStore>) ref;
        ModelComponent mc = store.getComponent(r, ModelComponent.getComponentType());
        Model model = mc != null ? mc.getModel() : null;
        Map<String, ?> sets = model != null ? model.getAnimationSetMap() : null;
        if (sets == null || sets.isEmpty()) return;

        String elegido = null;
        for (String cand : candidatos) {
            if (cand != null && sets.containsKey(cand)) {
                elegido = cand;
                break;
            }
        }
        if (elegido == null) return;

        try {
            NPCEntity npc = store.getComponent(r, NPCEntity.getComponentType());
            if (npc != null) {
                npc.playAnimation(r, slot, elegido, store);
            } else {
                AnimationUtils.playAnimation(r, slot, elegido, store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "AnimacionesCombate animar: " + e.getMessage());
        }
    }

    private static Ref<?> refDe(AlmacenJugadores.DatosJugador d, EstadoCombate c,
                                boolean aliado, int idx) {
        if (aliado) {
            return (d.refsBatalla != null && idx >= 0 && idx < d.refsBatalla.length)
                ? d.refsBatalla[idx] : null;
        }
        Enemigo e = c.enemigo(idx);
        return e != null ? e.ref : null;
    }
}
