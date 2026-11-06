package com.digitale.datos;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacén en memoria de datos de jugadores.
 * Dos compañeros Digimon por jugador (estilo Next Order).
 */
public class AlmacenJugadores {

    // ── Datos de jugador ───────────────────────────────────────────
    public static class DatosJugador {
        /** Equipo de hasta 6 Digimon. Los slots 0 y 1 son la pareja activa por defecto. */
        public static final int MAX_EQUIPO = 6;
        public final DatoDigimon[] equipo = new DatoDigimon[MAX_EQUIPO];

        /** Índices de la pareja activa (combate y paseo). */
        public int activoA = 0;
        public int activoB = 1;

        public boolean tieneEquipo = false;

        // Estado de combate activo
        public boolean enCombate     = false;
        public String  enemigoNombre = "";
        public String  enemigoElem   = "NEUTRO";
        public int     enemigoHp     = 0;
        public int     enemigoHpMax  = 0;
        public int     enemigoAtk    = 0;
        public int     enemigoDef    = 0;
        public int     enemigoExp    = 0;
        public int     turno         = 0;
        public String  logUltimo     = "";

        // Modo de combate Next Order: elegir táctica de cada compañero
        public String tacticaA = "BALANCEADO"; // AGRESIVO, BALANCEADO, DEFENSIVO
        public String tacticaB = "BALANCEADO";
        public String tacticaC = "BALANCEADO";

        // Estado de paseo — compañeros spawneados en el mundo (uno por slot)
        public boolean paseoActivo = false;
        public final Ref<?>[] refsPaseo = new Ref<?>[MAX_EQUIPO];

        /** ¿El slot i está paseando ahora mismo? */
        public boolean paseoEn(int i) {
            if (!slotValido(i)) return false;
            Ref<?> r = refsPaseo[i];
            return r != null && r.isValid();
        }

        public void setRefPaseo(int i, Ref<?> ref) {
            if (slotValido(i)) refsPaseo[i] = ref;
        }

        /** ¿Hay al menos un Digimon paseando? */
        public boolean algunPaseo() {
            for (int i = 0; i < MAX_EQUIPO; i++) if (paseoEn(i)) return true;
            return false;
        }

        /** Limpia todas las refs de paseo (al recoger). */
        public void limpiarPaseo() {
            for (int i = 0; i < MAX_EQUIPO; i++) refsPaseo[i] = null;
            paseoActivo = false;
        }

        // ── Refs de combate (NPCs spawneados para la formación, máx 3) ──
        public final Ref<?>[] refsBatalla = new Ref<?>[3];
        /** Especie que representa cada NPC de la formación (para el Cambio). */
        public final String[] refsBatallaEspecie = new String[3];

        public void limpiarBatalla() {
            for (int i = 0; i < refsBatalla.length; i++) {
                refsBatalla[i] = null;
                refsBatallaEspecie[i] = null;
            }
        }
        
        // Información de spawn guardada para rastreo
        public double posSpawnX = 0;
        public double posSpawnY = 0;
        public double posSpawnZ = 0;
        public String rolSpawnA = "";
        public String rolSpawnB = "";

        // Desafío PvP (se configura en el tab Batalla; de momento solo sesión)
        public boolean desafioAbierto = false;
        public String modoDesafio = "Amistoso";
        /** Reto pendiente: quién te retó (sesión). Null si nadie. */
        public UUID desafioDeUuid = null;
        public String desafioDeNombre = "";

        // ── Recetas desbloqueadas (Dato Incubación examinados, por bebé) ──
        public final java.util.Set<String> recetas = new java.util.HashSet<>();

        public boolean tieneReceta(String bebe) {
            return bebe != null && recetas.contains(bebe);
        }

        /** Retorna true si la desbloqueó ahora (era nueva). */
        public boolean desbloquearReceta(String bebe) {
            if (bebe == null || bebe.isEmpty()) return false;
            return recetas.add(bebe);
        }

        // ── Incubación activa en la Mesa (1 a la vez, con timestamp) ──
        // LEGADO (una sola incubación global; se migra a incubaciones por mesa).
        public String incubaHuevoId = "";
        public int incubaInicioSeg = 0;
        public int incubaCiclos = 0;
        public int incubaEnergia = 5;
        /** Especie objetivo fijada al combinar ("" = sorteo del pool al eclosionar). */
        public String incubaEspecie = "";
        /** % de éxito fijado al combinar (100 = receta completa). */
        public int incubaExitoPct = 100;
        /** ¿Ya se avisó de que está listo? (para no repetir). */
        public boolean incubaNotificado = false;
        /** Sobrealimentaciones seguidas del huevo en mesa (RAM; 3 = Burpmon). */
        public int sobrealimentacionCount = 0;

        /** Una incubación ligada a una mesa concreta (clave "x,y,z"). */
        public static class IncubacionMesa {
            public String huevoId = "";
            public String especie = "";
            public int exitoPct = 100;
            public int inicioSeg = 0;
            public int ciclos = 1;
            public int segCiclo = 60;
            public int energia = 5;
            public boolean notificado = false;
            public int sobrealim = 0;
        }

        /** Incubaciones por mesa (varias mesas en paralelo). */
        public final java.util.Map<String, IncubacionMesa> incubaciones = new java.util.LinkedHashMap<>();

        /** Banco de datos PC (crias reclamadas fuera del equipo). */
        public static final int BANCO_MAX = 100;
        public final java.util.List<DatoDigimon> banco = new java.util.ArrayList<>();

        public static String claveMesa(int x, int y, int z) {
            return x + "," + y + "," + z;
        }

        public void resetSobrealimentacion() {
            this.sobrealimentacionCount = 0;
        }

        /** ¿Hay una incubación en curso? */
        public boolean incubando() {
            return incubaHuevoId != null && !incubaHuevoId.isEmpty();
        }

        public void limpiarIncubacion() {
            incubaHuevoId = "";
            incubaInicioSeg = 0;
            incubaCiclos = 0;
            incubaEnergia = 5;
            incubaEspecie = "";
            incubaExitoPct = 100;
        }

        // ── Menú de combate por teclado (HUD, sin Page) ────────────
        /** ¿Menú de batalla abierto? (congela movimiento e input). */
        public boolean menuAbierto = false;
        /** Nivel: 0 = raíz, 1 = submenú. */
        public int menuNivel = 0;
        /** Índice del cursor en el nivel actual. */
        public int menuIdx = 0;
        /** Submenú abierto: "combate", "equipo", "tactica", "inventario" o "". */
        public String menuSub = "";
        /** Movimiento elegido a la espera de elegir objetivo (RAM). */
        public DatoDigimon.Movimiento menuMovimiento = null;
        /** Submenú desde el que se abrió la lista de objetivos. */
        public String menuSubAnterior = "";
        /** Para flancos: último salto visto, última dirección y último movimiento. */
        public boolean menuLastJump = false;
        public String menuLastDir = "";
        public long menuLastMoveMs = 0;

        // Sistema de batalla nuevo (UI-driven)
        public com.digitale.datos.EstadoCombate combate = null;
        /** Cámara de combate activa (RAM; no persiste). */
        public boolean camaraCombate = false;
        /** NPC invisible usado como ancla de camara en combate. */
        public Ref<?> refCamara;

        public boolean enCombateUI() {
            return combate != null && combate.activa;
        }

        public void iniciarCombate(String nombre, String elem,
                                   int hp, int atk, int def, int exp) {
            enCombate = true;
            enemigoNombre = nombre; enemigoElem = elem;
            enemigoHp = hp; enemigoHpMax = hp;
            enemigoAtk = atk; enemigoDef = def;
            enemigoExp = exp; turno = 1;
            logUltimo = "¡" + nombre + " quiere pelear!";
            DatoDigimon a = getA();
            DatoDigimon b = getB();
            if (a != null) a.hpCombate = a.hp;
            if (b != null) b.hpCombate = b.hp;
        }

        public void terminarCombate() {
            enCombate = false;
            enemigoNombre = ""; enemigoHp = 0; turno = 0;
            DatoDigimon a = getA();
            DatoDigimon b = getB();
            if (a != null) a.hp = Math.max(1, a.hpCombate);
            if (b != null) b.hp = Math.max(1, b.hpCombate);
        }

        public boolean ambosVivos() {
            DatoDigimon a = getA();
            DatoDigimon b = getB();
            return a != null && a.vivo && b != null && b.vivo;
        }

        public boolean alguienVivo() {
            DatoDigimon a = getA();
            DatoDigimon b = getB();
            return (a != null && a.vivo && a.hpCombate > 0) ||
                    (b != null && b.vivo && b.hpCombate > 0);
        }

        // ── Acceso al equipo ─────────────────────────────────────────
        /** Activo A (para combate/paseo). Null si el slot está vacío. */
        public DatoDigimon getA() {
            return slotValido(activoA) ? equipo[activoA] : null;
        }

        /** Activo B (para combate/paseo). Null si el slot está vacío. */
        public DatoDigimon getB() {
            return slotValido(activoB) ? equipo[activoB] : null;
        }

        public void setA(DatoDigimon d) {
            if (slotValido(activoA)) equipo[activoA] = d;
        }

        public void setB(DatoDigimon d) {
            if (slotValido(activoB)) equipo[activoB] = d;
        }

        public DatoDigimon getSlot(int i) {
            return slotValido(i) ? equipo[i] : null;
        }

        public void setSlot(int i, DatoDigimon d) {
            if (slotValido(i)) equipo[i] = d;
        }

        public static boolean slotValido(int i) {
            return i >= 0 && i < MAX_EQUIPO;
        }

        /** ¿El slot i es parte de la pareja activa? */
        public boolean esActivo(int i) {
            return i == activoA || i == activoB;
        }

        /** Marca el slot i como activo A (si no es el B actual). */
        public boolean elegirActivoA(int i) {
            if (!slotValido(i) || equipo[i] == null || i == activoB) return false;
            activoA = i;
            return true;
        }

        /** Marca el slot i como activo B (si no es el A actual). */
        public boolean elegirActivoB(int i) {
            if (!slotValido(i) || equipo[i] == null || i == activoA) return false;
            activoB = i;
            return true;
        }

        /** Intercambia los contenidos de dos slots. */
        public void intercambiarSlots(int i, int j) {
            if (!slotValido(i) || !slotValido(j) || i == j) return;
            DatoDigimon tmp = equipo[i];
            equipo[i] = equipo[j];
            equipo[j] = tmp;
        }

        /** Número de slots ocupados. */
        public int contarEquipo() {
            int n = 0;
            for (DatoDigimon d : equipo) if (d != null) n++;
            return n;
        }

        /** Primer slot vacío, o -1 si el equipo está lleno. */
        public int primerVacio() {
            for (int i = 0; i < MAX_EQUIPO; i++) if (equipo[i] == null) return i;
            return -1;
        }

        public boolean tieneEspacio() {
            return primerVacio() != -1;
        }

        /**
         * Equipo de combate (máx 3, estilo Story): los activos primero,
         * despiertos antes que dormidos. Los dormidos que entren lo hacen
         * con efecto DORMIDO (lo aplica el motor al iniciar).
         */
        public java.util.List<DatoDigimon> equipoCombate() {
            java.util.List<DatoDigimon> orden = new java.util.ArrayList<>();
            DatoDigimon a = getA();
            DatoDigimon b = getB();
            if (a != null) orden.add(a);
            if (b != null && b != a) orden.add(b);
            for (int i = 0; i < MAX_EQUIPO; i++) {
                DatoDigimon d = equipo[i];
                if (d != null && !orden.contains(d)) orden.add(d);
            }
            java.util.List<DatoDigimon> despiertos = new java.util.ArrayList<>();
            java.util.List<DatoDigimon> dormidos = new java.util.ArrayList<>();
            for (DatoDigimon d : orden) {
                if (d.vivo && !d.dormido && !d.noCombate) despiertos.add(d);
                else if (d.vivo && !d.noCombate) dormidos.add(d);
            }
            java.util.List<DatoDigimon> party = new java.util.ArrayList<>(despiertos);
            party.addAll(dormidos);
            return party.subList(0, Math.min(3, party.size()));
        }

        /** ¿Hay un Calumon vivo en el equipo? (potencia evoluciones). */
        public boolean tieneCalumon() {
            for (int i = 0; i < MAX_EQUIPO; i++) {
                DatoDigimon dd = equipo[i];
                if (dd != null && dd.vivo && "Calumon".equals(dd.especie)) return true;
            }
            return false;
        }

        public int bonoLazoCalumon() {
            return tieneCalumon() ? 15 : 0;
        }

        public int bonoVicCalumon() {
            return tieneCalumon() ? 1 : 0;
        }

        /** ¿Se puede iniciar un combate? (al menos un despierto). */
        public boolean puedeCombatir() {
            for (int i = 0; i < MAX_EQUIPO; i++) {
                DatoDigimon d = equipo[i];
                if (d != null && d.vivo && !d.dormido) return true;
            }
            return false;
        }

        /**
         * Hook para el sistema de incubación (mesas de trabajo): añade un Digimon
         * recién nacido al primer slot libre. Retorna el slot o -1 si está lleno.
         */
        public int agregarAlEquipo(DatoDigimon d) {
            int i = primerVacio();
            if (i == -1 || d == null) return -1;
            equipo[i] = d;
            tieneEquipo = true;
            return i;
        }
    }

    // ── Almacén estático ───────────────────────────────────────────
    private static final ConcurrentHashMap<UUID, DatosJugador> DATOS
        = new ConcurrentHashMap<>();

    public static DatosJugador obtener(UUID uuid) {
        return DATOS.computeIfAbsent(uuid, k -> new DatosJugador());
    }

    /** Alias de obtener() — retorna null si no existe aún el jugador */
    public static DatosJugador getDatos(UUID uuid) {
        return DATOS.get(uuid);
    }

    public static boolean existe(UUID uuid) {
        DatosJugador d = DATOS.get(uuid);
        return d != null && d.tieneEquipo;
    }
}
