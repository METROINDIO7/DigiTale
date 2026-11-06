package com.digitale.componentes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Componente persistente para almacenar el equipo y progreso completo del jugador.
 * Todo lo que esté aquí sobrevive entre sesiones.
 */
public class PetProgressComponent implements Component<EntityStore> {

    // ── Compañero A: identidad ────────────────────────────────────
    private String nombreA   = "";
    private String especieA  = "";
    private String elementoA = "NEUTRO";
    private int    nivelEvoA = 1;

    // ── Compañero A: stats ────────────────────────────────────────
    private int hpA = 40;    private int maxHpA = 40;
    private int atkA = 3;    private int defA = 3;
    private int spdA = 4;    private int wisA = 3;

    // ── Compañero A: entrenamiento ────────────────────────────────
    private int entAtkA = 0; private int entDefA = 0;
    private int entSpdA = 0; private int entWisA = 0;
    private int entHpA  = 0;

    // ── Compañero A: vinculo y estado ─────────────────────────────
    private int lazoA       = 20; private int disciplinaA = 20;
    private int victoriasA  = 0;  private int derrotasA   = 0;
    private boolean vivoA   = false;
    private boolean dormidoA = false; private int dormidoEnSegA = 0; private int vecesLlenoA = 0;
    private int hambreA     = 100; private int energiaA   = 100;
    private int abiA        = 0; private double crecimientoA = 1.0;

    // ── Compañero A: progreso (XP/nivel de aventura) ─────────────
    private int levelA      = 1;
    private int experienceA = 0;

    // ── Compañero B: identidad ────────────────────────────────────
    private String nombreB   = "";
    private String especieB  = "";
    private String elementoB = "NEUTRO";
    private int    nivelEvoB = 1;

    // ── Compañero B: stats ────────────────────────────────────────
    private int hpB = 40;    private int maxHpB = 40;
    private int atkB = 3;    private int defB = 3;
    private int spdB = 4;    private int wisB = 3;

    // ── Compañero B: entrenamiento ────────────────────────────────
    private int entAtkB = 0; private int entDefB = 0;
    private int entSpdB = 0; private int entWisB = 0;
    private int entHpB  = 0;

    // ── Compañero B: vinculo y estado ─────────────────────────────
    private int lazoB       = 20; private int disciplinaB = 20;
    private int victoriasB  = 0;  private int derrotasB   = 0;
    private boolean vivoB   = false;
    private boolean dormidoB = false; private int dormidoEnSegB = 0; private int vecesLlenoB = 0;
    private int hambreB     = 100; private int energiaB   = 100;
    private int abiB        = 0; private double crecimientoB = 1.0;

    // ── Compañero B: progreso (XP/nivel de aventura) ─────────────
    private int levelB      = 1;
    private int experienceB = 0;

    // ── Compañeros C-F: identidad ───────────────────────────────
    private String nombreC = ""; private String especieC = ""; private String elementoC = "NEUTRO"; private int nivelEvoC = 1;
    private String nombreD = ""; private String especieD = ""; private String elementoD = "NEUTRO"; private int nivelEvoD = 1;
    private String nombreE = ""; private String especieE = ""; private String elementoE = "NEUTRO"; private int nivelEvoE = 1;
    private String nombreF = ""; private String especieF = ""; private String elementoF = "NEUTRO"; private int nivelEvoF = 1;

    // ── Compañeros C-F: stats ───────────────────────────────────
    private int hpC = 40;    private int maxHpC = 40; private int atkC = 3; private int defC = 3; private int spdC = 4; private int wisC = 3;
    private int hpD = 40;    private int maxHpD = 40; private int atkD = 3; private int defD = 3; private int spdD = 4; private int wisD = 3;
    private int hpE = 40;    private int maxHpE = 40; private int atkE = 3; private int defE = 3; private int spdE = 4; private int wisE = 3;
    private int hpF = 40;    private int maxHpF = 40; private int atkF = 3; private int defF = 3; private int spdF = 4; private int wisF = 3;

    // ── Compañeros C-F: entrenamiento ───────────────────────────
    private int entAtkC = 0; private int entDefC = 0; private int entSpdC = 0; private int entWisC = 0; private int entHpC = 0;
    private int entAtkD = 0; private int entDefD = 0; private int entSpdD = 0; private int entWisD = 0; private int entHpD = 0;
    private int entAtkE = 0; private int entDefE = 0; private int entSpdE = 0; private int entWisE = 0; private int entHpE = 0;
    private int entAtkF = 0; private int entDefF = 0; private int entSpdF = 0; private int entWisF = 0; private int entHpF = 0;

    // ── Compañeros C-F: vínculo y estado ────────────────────────
    private int lazoC = 20; private int disciplinaC = 20; private int victoriasC = 0; private int derrotasC = 0;
    private boolean vivoC = false; private boolean dormidoC = false; private int dormidoEnSegC = 0; private int vecesLlenoC = 0; private int hambreC = 100; private int energiaC = 100; private int abiC = 0; private double crecimientoC = 1.0;
    private int lazoD = 20; private int disciplinaD = 20; private int victoriasD = 0; private int derrotasD = 0;
    private boolean vivoD = false; private boolean dormidoD = false; private int dormidoEnSegD = 0; private int vecesLlenoD = 0; private int hambreD = 100; private int energiaD = 100; private int abiD = 0; private double crecimientoD = 1.0;
    private int lazoE = 20; private int disciplinaE = 20; private int victoriasE = 0; private int derrotasE = 0;
    private boolean vivoE = false; private boolean dormidoE = false; private int dormidoEnSegE = 0; private int vecesLlenoE = 0; private int hambreE = 100; private int energiaE = 100; private int abiE = 0; private double crecimientoE = 1.0;
    private int lazoF = 20; private int disciplinaF = 20; private int victoriasF = 0; private int derrotasF = 0;
    private boolean vivoF = false; private boolean dormidoF = false; private int dormidoEnSegF = 0; private int vecesLlenoF = 0; private int hambreF = 100; private int energiaF = 100; private int abiF = 0; private double crecimientoF = 1.0;

    // ── Compañeros C-F: progreso ────────────────────────────────
    private int levelC = 1; private int experienceC = 0;
    private int levelD = 1; private int experienceD = 0;
    private int levelE = 1; private int experienceE = 0;
    private int levelF = 1; private int experienceF = 0;

    // ── Incubación activa ─────────────────────────────────────────
    private String incubaHuevoId = "";
    private int incubaInicioSeg = 0;
    private int incubaCiclos = 0;
    private int incubaEnergia = 5;
    private boolean incubaNotificado = false;
    private String incubaEspecie = "";
    private int incubaExitoPct = 100;
    /** Incubaciones por mesa serializadas: clave|huevo|especie|pct|inicio|ciclos|segCiclo|energia|notif|sobrealim;... */
    private String incubacionesMesa = "";
    /** Banco PC serializado (gson de List<DatoDigimon>). */
    private String bancoDigimon = "";

    // ── Recetas desbloqueadas (CSV de bebés) ──────────────────────
    private String recetasDesbloqueadas = "";

    // ── Flags de equipo ───────────────────────────────────────────
    private boolean tieneEquipo = false;

    // ═════════════════════════════════════════════════════════════
    // CODEC
    // ═════════════════════════════════════════════════════════════
    public static final BuilderCodec<PetProgressComponent> CODEC = BuilderCodec.builder(
                    PetProgressComponent.class, PetProgressComponent::new)
            // ── A identidad
            .append(new KeyedCodec<>("NombreA",   Codec.STRING),  (d,v)->d.nombreA=v,   d->d.nombreA).add()
            .append(new KeyedCodec<>("EspecieA",  Codec.STRING),  (d,v)->d.especieA=v,  d->d.especieA).add()
            .append(new KeyedCodec<>("ElementoA", Codec.STRING),  (d,v)->d.elementoA=v, d->d.elementoA).add()
            .append(new KeyedCodec<>("NivelEvoA", Codec.INTEGER), (d,v)->d.nivelEvoA=v, d->d.nivelEvoA).add()
            // ── A stats
            .append(new KeyedCodec<>("HpA",    Codec.INTEGER), (d,v)->d.hpA=v,    d->d.hpA).add()
            .append(new KeyedCodec<>("MaxHpA", Codec.INTEGER), (d,v)->d.maxHpA=v, d->d.maxHpA).add()
            .append(new KeyedCodec<>("AtkA",   Codec.INTEGER), (d,v)->d.atkA=v,   d->d.atkA).add()
            .append(new KeyedCodec<>("DefA",   Codec.INTEGER), (d,v)->d.defA=v,   d->d.defA).add()
            .append(new KeyedCodec<>("SpdA",   Codec.INTEGER), (d,v)->d.spdA=v,   d->d.spdA).add()
            .append(new KeyedCodec<>("WisA",   Codec.INTEGER), (d,v)->d.wisA=v,   d->d.wisA).add()
            // ── A entrenamiento
            .append(new KeyedCodec<>("EntAtkA", Codec.INTEGER), (d,v)->d.entAtkA=v, d->d.entAtkA).add()
            .append(new KeyedCodec<>("EntDefA", Codec.INTEGER), (d,v)->d.entDefA=v, d->d.entDefA).add()
            .append(new KeyedCodec<>("EntSpdA", Codec.INTEGER), (d,v)->d.entSpdA=v, d->d.entSpdA).add()
            .append(new KeyedCodec<>("EntWisA", Codec.INTEGER), (d,v)->d.entWisA=v, d->d.entWisA).add()
            .append(new KeyedCodec<>("EntHpA",  Codec.INTEGER), (d,v)->d.entHpA=v,  d->d.entHpA).add()
            // ── A vinculo
            .append(new KeyedCodec<>("LazoA",       Codec.INTEGER), (d,v)->d.lazoA=v,       d->d.lazoA).add()
            .append(new KeyedCodec<>("DisciplinaA", Codec.INTEGER), (d,v)->d.disciplinaA=v, d->d.disciplinaA).add()
            .append(new KeyedCodec<>("VictoriasA",  Codec.INTEGER), (d,v)->d.victoriasA=v,  d->d.victoriasA).add()
            .append(new KeyedCodec<>("DerrotasA",   Codec.INTEGER), (d,v)->d.derrotasA=v,   d->d.derrotasA).add()
            .append(new KeyedCodec<>("VivoA",       Codec.BOOLEAN), (d,v)->d.vivoA=v,       d->d.vivoA).add()
            .append(new KeyedCodec<>("DormidoA",   Codec.BOOLEAN), (d,v)->d.dormidoA=v,   d->d.dormidoA).add()
            .append(new KeyedCodec<>("DormidoEnSegA", Codec.INTEGER), (d,v)->d.dormidoEnSegA=v, d->d.dormidoEnSegA).add()
            .append(new KeyedCodec<>("VecesLlenoA", Codec.INTEGER), (d,v)->d.vecesLlenoA=v, d->d.vecesLlenoA).add()
            .append(new KeyedCodec<>("HambreA",     Codec.INTEGER), (d,v)->d.hambreA=v,     d->d.hambreA).add()
            .append(new KeyedCodec<>("EnergiaA",    Codec.INTEGER), (d,v)->d.energiaA=v,    d->d.energiaA).add()
            .append(new KeyedCodec<>("AbiA",        Codec.INTEGER), (d,v)->d.abiA=v,        d->d.abiA).add()
            .append(new KeyedCodec<>("CrecimientoA", Codec.DOUBLE), (d,v)->d.crecimientoA=v, d->d.crecimientoA).add()
            // ── A progreso
            .append(new KeyedCodec<>("LevelA",      Codec.INTEGER), (d,v)->d.levelA=v,      d->d.levelA).add()
            .append(new KeyedCodec<>("ExperienceA", Codec.INTEGER), (d,v)->d.experienceA=v, d->d.experienceA).add()
            // ── B identidad
            .append(new KeyedCodec<>("NombreB",   Codec.STRING),  (d,v)->d.nombreB=v,   d->d.nombreB).add()
            .append(new KeyedCodec<>("EspecieB",  Codec.STRING),  (d,v)->d.especieB=v,  d->d.especieB).add()
            .append(new KeyedCodec<>("ElementoB", Codec.STRING),  (d,v)->d.elementoB=v, d->d.elementoB).add()
            .append(new KeyedCodec<>("NivelEvoB", Codec.INTEGER), (d,v)->d.nivelEvoB=v, d->d.nivelEvoB).add()
            // ── B stats
            .append(new KeyedCodec<>("HpB",    Codec.INTEGER), (d,v)->d.hpB=v,    d->d.hpB).add()
            .append(new KeyedCodec<>("MaxHpB", Codec.INTEGER), (d,v)->d.maxHpB=v, d->d.maxHpB).add()
            .append(new KeyedCodec<>("AtkB",   Codec.INTEGER), (d,v)->d.atkB=v,   d->d.atkB).add()
            .append(new KeyedCodec<>("DefB",   Codec.INTEGER), (d,v)->d.defB=v,   d->d.defB).add()
            .append(new KeyedCodec<>("SpdB",   Codec.INTEGER), (d,v)->d.spdB=v,   d->d.spdB).add()
            .append(new KeyedCodec<>("WisB",   Codec.INTEGER), (d,v)->d.wisB=v,   d->d.wisB).add()
            // ── B entrenamiento
            .append(new KeyedCodec<>("EntAtkB", Codec.INTEGER), (d,v)->d.entAtkB=v, d->d.entAtkB).add()
            .append(new KeyedCodec<>("EntDefB", Codec.INTEGER), (d,v)->d.entDefB=v, d->d.entDefB).add()
            .append(new KeyedCodec<>("EntSpdB", Codec.INTEGER), (d,v)->d.entSpdB=v, d->d.entSpdB).add()
            .append(new KeyedCodec<>("EntWisB", Codec.INTEGER), (d,v)->d.entWisB=v, d->d.entWisB).add()
            .append(new KeyedCodec<>("EntHpB",  Codec.INTEGER), (d,v)->d.entHpB=v,  d->d.entHpB).add()
            // ── B vinculo
            .append(new KeyedCodec<>("LazoB",       Codec.INTEGER), (d,v)->d.lazoB=v,       d->d.lazoB).add()
            .append(new KeyedCodec<>("DisciplinaB", Codec.INTEGER), (d,v)->d.disciplinaB=v, d->d.disciplinaB).add()
            .append(new KeyedCodec<>("VictoriasB",  Codec.INTEGER), (d,v)->d.victoriasB=v,  d->d.victoriasB).add()
            .append(new KeyedCodec<>("DerrotasB",   Codec.INTEGER), (d,v)->d.derrotasB=v,   d->d.derrotasB).add()
            .append(new KeyedCodec<>("VivoB",       Codec.BOOLEAN), (d,v)->d.vivoB=v,       d->d.vivoB).add()
            .append(new KeyedCodec<>("DormidoB",   Codec.BOOLEAN), (d,v)->d.dormidoB=v,   d->d.dormidoB).add()
            .append(new KeyedCodec<>("DormidoEnSegB", Codec.INTEGER), (d,v)->d.dormidoEnSegB=v, d->d.dormidoEnSegB).add()
            .append(new KeyedCodec<>("VecesLlenoB", Codec.INTEGER), (d,v)->d.vecesLlenoB=v, d->d.vecesLlenoB).add()
            .append(new KeyedCodec<>("HambreB",     Codec.INTEGER), (d,v)->d.hambreB=v,     d->d.hambreB).add()
            .append(new KeyedCodec<>("EnergiaB",    Codec.INTEGER), (d,v)->d.energiaB=v,    d->d.energiaB).add()
            .append(new KeyedCodec<>("AbiB",        Codec.INTEGER), (d,v)->d.abiB=v,        d->d.abiB).add()
            .append(new KeyedCodec<>("CrecimientoB", Codec.DOUBLE), (d,v)->d.crecimientoB=v, d->d.crecimientoB).add()
            // ── B progreso
            .append(new KeyedCodec<>("LevelB",      Codec.INTEGER), (d,v)->d.levelB=v,      d->d.levelB).add()
            .append(new KeyedCodec<>("ExperienceB", Codec.INTEGER), (d,v)->d.experienceB=v, d->d.experienceB).add()
            // ── C identidad/stats/entrenamiento/vínculo/progreso
            .append(new KeyedCodec<>("NombreC",   Codec.STRING),  (d,v)->d.nombreC=v,   d->d.nombreC).add()
            .append(new KeyedCodec<>("EspecieC",  Codec.STRING),  (d,v)->d.especieC=v,  d->d.especieC).add()
            .append(new KeyedCodec<>("ElementoC", Codec.STRING),  (d,v)->d.elementoC=v, d->d.elementoC).add()
            .append(new KeyedCodec<>("NivelEvoC", Codec.INTEGER), (d,v)->d.nivelEvoC=v, d->d.nivelEvoC).add()
            .append(new KeyedCodec<>("HpC",    Codec.INTEGER), (d,v)->d.hpC=v,    d->d.hpC).add()
            .append(new KeyedCodec<>("MaxHpC", Codec.INTEGER), (d,v)->d.maxHpC=v, d->d.maxHpC).add()
            .append(new KeyedCodec<>("AtkC",   Codec.INTEGER), (d,v)->d.atkC=v,   d->d.atkC).add()
            .append(new KeyedCodec<>("DefC",   Codec.INTEGER), (d,v)->d.defC=v,   d->d.defC).add()
            .append(new KeyedCodec<>("SpdC",   Codec.INTEGER), (d,v)->d.spdC=v,   d->d.spdC).add()
            .append(new KeyedCodec<>("WisC",   Codec.INTEGER), (d,v)->d.wisC=v,   d->d.wisC).add()
            .append(new KeyedCodec<>("EntAtkC", Codec.INTEGER), (d,v)->d.entAtkC=v, d->d.entAtkC).add()
            .append(new KeyedCodec<>("EntDefC", Codec.INTEGER), (d,v)->d.entDefC=v, d->d.entDefC).add()
            .append(new KeyedCodec<>("EntSpdC", Codec.INTEGER), (d,v)->d.entSpdC=v, d->d.entSpdC).add()
            .append(new KeyedCodec<>("EntWisC", Codec.INTEGER), (d,v)->d.entWisC=v, d->d.entWisC).add()
            .append(new KeyedCodec<>("EntHpC",  Codec.INTEGER), (d,v)->d.entHpC=v,  d->d.entHpC).add()
            .append(new KeyedCodec<>("LazoC",       Codec.INTEGER), (d,v)->d.lazoC=v,       d->d.lazoC).add()
            .append(new KeyedCodec<>("DisciplinaC", Codec.INTEGER), (d,v)->d.disciplinaC=v, d->d.disciplinaC).add()
            .append(new KeyedCodec<>("VictoriasC",  Codec.INTEGER), (d,v)->d.victoriasC=v,  d->d.victoriasC).add()
            .append(new KeyedCodec<>("DerrotasC",   Codec.INTEGER), (d,v)->d.derrotasC=v,   d->d.derrotasC).add()
            .append(new KeyedCodec<>("VivoC",       Codec.BOOLEAN), (d,v)->d.vivoC=v,       d->d.vivoC).add()
            .append(new KeyedCodec<>("DormidoC",   Codec.BOOLEAN), (d,v)->d.dormidoC=v,   d->d.dormidoC).add()
            .append(new KeyedCodec<>("DormidoEnSegC", Codec.INTEGER), (d,v)->d.dormidoEnSegC=v, d->d.dormidoEnSegC).add()
            .append(new KeyedCodec<>("VecesLlenoC", Codec.INTEGER), (d,v)->d.vecesLlenoC=v, d->d.vecesLlenoC).add()
            .append(new KeyedCodec<>("HambreC",     Codec.INTEGER), (d,v)->d.hambreC=v,     d->d.hambreC).add()
            .append(new KeyedCodec<>("EnergiaC",    Codec.INTEGER), (d,v)->d.energiaC=v,    d->d.energiaC).add()
            .append(new KeyedCodec<>("AbiC",        Codec.INTEGER), (d,v)->d.abiC=v,        d->d.abiC).add()
            .append(new KeyedCodec<>("CrecimientoC", Codec.DOUBLE), (d,v)->d.crecimientoC=v, d->d.crecimientoC).add()
            .append(new KeyedCodec<>("LevelC",      Codec.INTEGER), (d,v)->d.levelC=v,      d->d.levelC).add()
            .append(new KeyedCodec<>("ExperienceC", Codec.INTEGER), (d,v)->d.experienceC=v, d->d.experienceC).add()
            // ── D identidad/stats/entrenamiento/vínculo/progreso
            .append(new KeyedCodec<>("NombreD",   Codec.STRING),  (d,v)->d.nombreD=v,   d->d.nombreD).add()
            .append(new KeyedCodec<>("EspecieD",  Codec.STRING),  (d,v)->d.especieD=v,  d->d.especieD).add()
            .append(new KeyedCodec<>("ElementoD", Codec.STRING),  (d,v)->d.elementoD=v, d->d.elementoD).add()
            .append(new KeyedCodec<>("NivelEvoD", Codec.INTEGER), (d,v)->d.nivelEvoD=v, d->d.nivelEvoD).add()
            .append(new KeyedCodec<>("HpD",    Codec.INTEGER), (d,v)->d.hpD=v,    d->d.hpD).add()
            .append(new KeyedCodec<>("MaxHpD", Codec.INTEGER), (d,v)->d.maxHpD=v, d->d.maxHpD).add()
            .append(new KeyedCodec<>("AtkD",   Codec.INTEGER), (d,v)->d.atkD=v,   d->d.atkD).add()
            .append(new KeyedCodec<>("DefD",   Codec.INTEGER), (d,v)->d.defD=v,   d->d.defD).add()
            .append(new KeyedCodec<>("SpdD",   Codec.INTEGER), (d,v)->d.spdD=v,   d->d.spdD).add()
            .append(new KeyedCodec<>("WisD",   Codec.INTEGER), (d,v)->d.wisD=v,   d->d.wisD).add()
            .append(new KeyedCodec<>("EntAtkD", Codec.INTEGER), (d,v)->d.entAtkD=v, d->d.entAtkD).add()
            .append(new KeyedCodec<>("EntDefD", Codec.INTEGER), (d,v)->d.entDefD=v, d->d.entDefD).add()
            .append(new KeyedCodec<>("EntSpdD", Codec.INTEGER), (d,v)->d.entSpdD=v, d->d.entSpdD).add()
            .append(new KeyedCodec<>("EntWisD", Codec.INTEGER), (d,v)->d.entWisD=v, d->d.entWisD).add()
            .append(new KeyedCodec<>("EntHpD",  Codec.INTEGER), (d,v)->d.entHpD=v,  d->d.entHpD).add()
            .append(new KeyedCodec<>("LazoD",       Codec.INTEGER), (d,v)->d.lazoD=v,       d->d.lazoD).add()
            .append(new KeyedCodec<>("DisciplinaD", Codec.INTEGER), (d,v)->d.disciplinaD=v, d->d.disciplinaD).add()
            .append(new KeyedCodec<>("VictoriasD",  Codec.INTEGER), (d,v)->d.victoriasD=v,  d->d.victoriasD).add()
            .append(new KeyedCodec<>("DerrotasD",   Codec.INTEGER), (d,v)->d.derrotasD=v,   d->d.derrotasD).add()
            .append(new KeyedCodec<>("VivoD",       Codec.BOOLEAN), (d,v)->d.vivoD=v,       d->d.vivoD).add()
            .append(new KeyedCodec<>("DormidoD",   Codec.BOOLEAN), (d,v)->d.dormidoD=v,   d->d.dormidoD).add()
            .append(new KeyedCodec<>("DormidoEnSegD", Codec.INTEGER), (d,v)->d.dormidoEnSegD=v, d->d.dormidoEnSegD).add()
            .append(new KeyedCodec<>("VecesLlenoD", Codec.INTEGER), (d,v)->d.vecesLlenoD=v, d->d.vecesLlenoD).add()
            .append(new KeyedCodec<>("HambreD",     Codec.INTEGER), (d,v)->d.hambreD=v,     d->d.hambreD).add()
            .append(new KeyedCodec<>("EnergiaD",    Codec.INTEGER), (d,v)->d.energiaD=v,    d->d.energiaD).add()
            .append(new KeyedCodec<>("AbiD",        Codec.INTEGER), (d,v)->d.abiD=v,        d->d.abiD).add()
            .append(new KeyedCodec<>("CrecimientoD", Codec.DOUBLE), (d,v)->d.crecimientoD=v, d->d.crecimientoD).add()
            .append(new KeyedCodec<>("LevelD",      Codec.INTEGER), (d,v)->d.levelD=v,      d->d.levelD).add()
            .append(new KeyedCodec<>("ExperienceD", Codec.INTEGER), (d,v)->d.experienceD=v, d->d.experienceD).add()
            // ── E identidad/stats/entrenamiento/vínculo/progreso
            .append(new KeyedCodec<>("NombreE",   Codec.STRING),  (d,v)->d.nombreE=v,   d->d.nombreE).add()
            .append(new KeyedCodec<>("EspecieE",  Codec.STRING),  (d,v)->d.especieE=v,  d->d.especieE).add()
            .append(new KeyedCodec<>("ElementoE", Codec.STRING),  (d,v)->d.elementoE=v, d->d.elementoE).add()
            .append(new KeyedCodec<>("NivelEvoE", Codec.INTEGER), (d,v)->d.nivelEvoE=v, d->d.nivelEvoE).add()
            .append(new KeyedCodec<>("HpE",    Codec.INTEGER), (d,v)->d.hpE=v,    d->d.hpE).add()
            .append(new KeyedCodec<>("MaxHpE", Codec.INTEGER), (d,v)->d.maxHpE=v, d->d.maxHpE).add()
            .append(new KeyedCodec<>("AtkE",   Codec.INTEGER), (d,v)->d.atkE=v,   d->d.atkE).add()
            .append(new KeyedCodec<>("DefE",   Codec.INTEGER), (d,v)->d.defE=v,   d->d.defE).add()
            .append(new KeyedCodec<>("SpdE",   Codec.INTEGER), (d,v)->d.spdE=v,   d->d.spdE).add()
            .append(new KeyedCodec<>("WisE",   Codec.INTEGER), (d,v)->d.wisE=v,   d->d.wisE).add()
            .append(new KeyedCodec<>("EntAtkE", Codec.INTEGER), (d,v)->d.entAtkE=v, d->d.entAtkE).add()
            .append(new KeyedCodec<>("EntDefE", Codec.INTEGER), (d,v)->d.entDefE=v, d->d.entDefE).add()
            .append(new KeyedCodec<>("EntSpdE", Codec.INTEGER), (d,v)->d.entSpdE=v, d->d.entSpdE).add()
            .append(new KeyedCodec<>("EntWisE", Codec.INTEGER), (d,v)->d.entWisE=v, d->d.entWisE).add()
            .append(new KeyedCodec<>("EntHpE",  Codec.INTEGER), (d,v)->d.entHpE=v,  d->d.entHpE).add()
            .append(new KeyedCodec<>("LazoE",       Codec.INTEGER), (d,v)->d.lazoE=v,       d->d.lazoE).add()
            .append(new KeyedCodec<>("DisciplinaE", Codec.INTEGER), (d,v)->d.disciplinaE=v, d->d.disciplinaE).add()
            .append(new KeyedCodec<>("VictoriasE",  Codec.INTEGER), (d,v)->d.victoriasE=v,  d->d.victoriasE).add()
            .append(new KeyedCodec<>("DerrotasE",   Codec.INTEGER), (d,v)->d.derrotasE=v,   d->d.derrotasE).add()
            .append(new KeyedCodec<>("VivoE",       Codec.BOOLEAN), (d,v)->d.vivoE=v,       d->d.vivoE).add()
            .append(new KeyedCodec<>("DormidoE",   Codec.BOOLEAN), (d,v)->d.dormidoE=v,   d->d.dormidoE).add()
            .append(new KeyedCodec<>("DormidoEnSegE", Codec.INTEGER), (d,v)->d.dormidoEnSegE=v, d->d.dormidoEnSegE).add()
            .append(new KeyedCodec<>("VecesLlenoE", Codec.INTEGER), (d,v)->d.vecesLlenoE=v, d->d.vecesLlenoE).add()
            .append(new KeyedCodec<>("HambreE",     Codec.INTEGER), (d,v)->d.hambreE=v,     d->d.hambreE).add()
            .append(new KeyedCodec<>("EnergiaE",    Codec.INTEGER), (d,v)->d.energiaE=v,    d->d.energiaE).add()
            .append(new KeyedCodec<>("AbiE",        Codec.INTEGER), (d,v)->d.abiE=v,        d->d.abiE).add()
            .append(new KeyedCodec<>("CrecimientoE", Codec.DOUBLE), (d,v)->d.crecimientoE=v, d->d.crecimientoE).add()
            .append(new KeyedCodec<>("LevelE",      Codec.INTEGER), (d,v)->d.levelE=v,      d->d.levelE).add()
            .append(new KeyedCodec<>("ExperienceE", Codec.INTEGER), (d,v)->d.experienceE=v, d->d.experienceE).add()
            // ── F identidad/stats/entrenamiento/vínculo/progreso
            .append(new KeyedCodec<>("NombreF",   Codec.STRING),  (d,v)->d.nombreF=v,   d->d.nombreF).add()
            .append(new KeyedCodec<>("EspecieF",  Codec.STRING),  (d,v)->d.especieF=v,  d->d.especieF).add()
            .append(new KeyedCodec<>("ElementoF", Codec.STRING),  (d,v)->d.elementoF=v, d->d.elementoF).add()
            .append(new KeyedCodec<>("NivelEvoF", Codec.INTEGER), (d,v)->d.nivelEvoF=v, d->d.nivelEvoF).add()
            .append(new KeyedCodec<>("HpF",    Codec.INTEGER), (d,v)->d.hpF=v,    d->d.hpF).add()
            .append(new KeyedCodec<>("MaxHpF", Codec.INTEGER), (d,v)->d.maxHpF=v, d->d.maxHpF).add()
            .append(new KeyedCodec<>("AtkF",   Codec.INTEGER), (d,v)->d.atkF=v,   d->d.atkF).add()
            .append(new KeyedCodec<>("DefF",   Codec.INTEGER), (d,v)->d.defF=v,   d->d.defF).add()
            .append(new KeyedCodec<>("SpdF",   Codec.INTEGER), (d,v)->d.spdF=v,   d->d.spdF).add()
            .append(new KeyedCodec<>("WisF",   Codec.INTEGER), (d,v)->d.wisF=v,   d->d.wisF).add()
            .append(new KeyedCodec<>("EntAtkF", Codec.INTEGER), (d,v)->d.entAtkF=v, d->d.entAtkF).add()
            .append(new KeyedCodec<>("EntDefF", Codec.INTEGER), (d,v)->d.entDefF=v, d->d.entDefF).add()
            .append(new KeyedCodec<>("EntSpdF", Codec.INTEGER), (d,v)->d.entSpdF=v, d->d.entSpdF).add()
            .append(new KeyedCodec<>("EntWisF", Codec.INTEGER), (d,v)->d.entWisF=v, d->d.entWisF).add()
            .append(new KeyedCodec<>("EntHpF",  Codec.INTEGER), (d,v)->d.entHpF=v,  d->d.entHpF).add()
            .append(new KeyedCodec<>("LazoF",       Codec.INTEGER), (d,v)->d.lazoF=v,       d->d.lazoF).add()
            .append(new KeyedCodec<>("DisciplinaF", Codec.INTEGER), (d,v)->d.disciplinaF=v, d->d.disciplinaF).add()
            .append(new KeyedCodec<>("VictoriasF",  Codec.INTEGER), (d,v)->d.victoriasF=v,  d->d.victoriasF).add()
            .append(new KeyedCodec<>("DerrotasF",   Codec.INTEGER), (d,v)->d.derrotasF=v,   d->d.derrotasF).add()
            .append(new KeyedCodec<>("VivoF",       Codec.BOOLEAN), (d,v)->d.vivoF=v,       d->d.vivoF).add()
            .append(new KeyedCodec<>("DormidoF",   Codec.BOOLEAN), (d,v)->d.dormidoF=v,   d->d.dormidoF).add()
            .append(new KeyedCodec<>("DormidoEnSegF", Codec.INTEGER), (d,v)->d.dormidoEnSegF=v, d->d.dormidoEnSegF).add()
            .append(new KeyedCodec<>("VecesLlenoF", Codec.INTEGER), (d,v)->d.vecesLlenoF=v, d->d.vecesLlenoF).add()
            .append(new KeyedCodec<>("HambreF",     Codec.INTEGER), (d,v)->d.hambreF=v,     d->d.hambreF).add()
            .append(new KeyedCodec<>("EnergiaF",    Codec.INTEGER), (d,v)->d.energiaF=v,    d->d.energiaF).add()
            .append(new KeyedCodec<>("AbiF",        Codec.INTEGER), (d,v)->d.abiF=v,        d->d.abiF).add()
            .append(new KeyedCodec<>("CrecimientoF", Codec.DOUBLE), (d,v)->d.crecimientoF=v, d->d.crecimientoF).add()
            .append(new KeyedCodec<>("LevelF",      Codec.INTEGER), (d,v)->d.levelF=v,      d->d.levelF).add()
            .append(new KeyedCodec<>("ExperienceF", Codec.INTEGER), (d,v)->d.experienceF=v, d->d.experienceF).add()
            // ── incubación
            .append(new KeyedCodec<>("IncubaHuevoId", Codec.STRING),  (d,v)->d.incubaHuevoId=v,   d->d.incubaHuevoId).add()
            .append(new KeyedCodec<>("IncubaInicioSeg", Codec.INTEGER), (d,v)->d.incubaInicioSeg=v, d->d.incubaInicioSeg).add()
            .append(new KeyedCodec<>("IncubaCiclos", Codec.INTEGER), (d,v)->d.incubaCiclos=v, d->d.incubaCiclos).add()
            .append(new KeyedCodec<>("IncubaEnergia", Codec.INTEGER), (d,v)->d.incubaEnergia=v, d->d.incubaEnergia).add()
            .append(new KeyedCodec<>("IncubaNotificado", Codec.BOOLEAN), (d,v)->d.incubaNotificado=v, d->d.incubaNotificado).add()
            .append(new KeyedCodec<>("IncubaEspecie", Codec.STRING),  (d,v)->d.incubaEspecie=v,   d->d.incubaEspecie).add()
            .append(new KeyedCodec<>("IncubaExitoPct", Codec.INTEGER), (d,v)->d.incubaExitoPct=v, d->d.incubaExitoPct).add()
            .append(new KeyedCodec<>("IncubacionesMesa", Codec.STRING),  (d,v)->d.incubacionesMesa=v,   d->d.incubacionesMesa).add()
            .append(new KeyedCodec<>("BancoDigimon", Codec.STRING),  (d,v)->d.bancoDigimon=v,   d->d.bancoDigimon).add()
            // ── recetas
            .append(new KeyedCodec<>("RecetasDesbloqueadas", Codec.STRING), (d,v)->d.recetasDesbloqueadas=v, d->d.recetasDesbloqueadas).add()
            // ── flags
            .append(new KeyedCodec<>("TieneEquipo", Codec.BOOLEAN), (d,v)->d.tieneEquipo=v, d->d.tieneEquipo).add()
            .build();

    public PetProgressComponent() {}

    public PetProgressComponent(PetProgressComponent o) {
        this.nombreA=o.nombreA; this.especieA=o.especieA; this.elementoA=o.elementoA; this.nivelEvoA=o.nivelEvoA;
        this.hpA=o.hpA; this.maxHpA=o.maxHpA; this.atkA=o.atkA; this.defA=o.defA; this.spdA=o.spdA; this.wisA=o.wisA;
        this.entAtkA=o.entAtkA; this.entDefA=o.entDefA; this.entSpdA=o.entSpdA; this.entWisA=o.entWisA; this.entHpA=o.entHpA;
        this.lazoA=o.lazoA; this.disciplinaA=o.disciplinaA; this.victoriasA=o.victoriasA; this.derrotasA=o.derrotasA;
        this.vivoA=o.vivoA; this.dormidoA=o.dormidoA; this.dormidoEnSegA=o.dormidoEnSegA; this.vecesLlenoA=o.vecesLlenoA; this.hambreA=o.hambreA; this.energiaA=o.energiaA; this.abiA=o.abiA; this.crecimientoA=o.crecimientoA;
        this.levelA=o.levelA; this.experienceA=o.experienceA;

        this.nombreB=o.nombreB; this.especieB=o.especieB; this.elementoB=o.elementoB; this.nivelEvoB=o.nivelEvoB;
        this.hpB=o.hpB; this.maxHpB=o.maxHpB; this.atkB=o.atkB; this.defB=o.defB; this.spdB=o.spdB; this.wisB=o.wisB;
        this.entAtkB=o.entAtkB; this.entDefB=o.entDefB; this.entSpdB=o.entSpdB; this.entWisB=o.entWisB; this.entHpB=o.entHpB;
        this.lazoB=o.lazoB; this.disciplinaB=o.disciplinaB; this.victoriasB=o.victoriasB; this.derrotasB=o.derrotasB;
        this.vivoB=o.vivoB; this.dormidoB=o.dormidoB; this.dormidoEnSegB=o.dormidoEnSegB; this.vecesLlenoB=o.vecesLlenoB; this.hambreB=o.hambreB; this.energiaB=o.energiaB; this.abiB=o.abiB; this.crecimientoB=o.crecimientoB;
        this.levelB=o.levelB; this.experienceB=o.experienceB;

        this.nombreC=o.nombreC; this.especieC=o.especieC; this.elementoC=o.elementoC; this.nivelEvoC=o.nivelEvoC;
        this.hpC=o.hpC; this.maxHpC=o.maxHpC; this.atkC=o.atkC; this.defC=o.defC; this.spdC=o.spdC; this.wisC=o.wisC;
        this.entAtkC=o.entAtkC; this.entDefC=o.entDefC; this.entSpdC=o.entSpdC; this.entWisC=o.entWisC; this.entHpC=o.entHpC;
        this.lazoC=o.lazoC; this.disciplinaC=o.disciplinaC; this.victoriasC=o.victoriasC; this.derrotasC=o.derrotasC;
        this.vivoC=o.vivoC; this.dormidoC=o.dormidoC; this.dormidoEnSegC=o.dormidoEnSegC; this.vecesLlenoC=o.vecesLlenoC; this.hambreC=o.hambreC; this.energiaC=o.energiaC; this.abiC=o.abiC; this.crecimientoC=o.crecimientoC;
        this.levelC=o.levelC; this.experienceC=o.experienceC;

        this.nombreD=o.nombreD; this.especieD=o.especieD; this.elementoD=o.elementoD; this.nivelEvoD=o.nivelEvoD;
        this.hpD=o.hpD; this.maxHpD=o.maxHpD; this.atkD=o.atkD; this.defD=o.defD; this.spdD=o.spdD; this.wisD=o.wisD;
        this.entAtkD=o.entAtkD; this.entDefD=o.entDefD; this.entSpdD=o.entSpdD; this.entWisD=o.entWisD; this.entHpD=o.entHpD;
        this.lazoD=o.lazoD; this.disciplinaD=o.disciplinaD; this.victoriasD=o.victoriasD; this.derrotasD=o.derrotasD;
        this.vivoD=o.vivoD; this.dormidoD=o.dormidoD; this.dormidoEnSegD=o.dormidoEnSegD; this.vecesLlenoD=o.vecesLlenoD; this.hambreD=o.hambreD; this.energiaD=o.energiaD; this.abiD=o.abiD; this.crecimientoD=o.crecimientoD;
        this.levelD=o.levelD; this.experienceD=o.experienceD;

        this.nombreE=o.nombreE; this.especieE=o.especieE; this.elementoE=o.elementoE; this.nivelEvoE=o.nivelEvoE;
        this.hpE=o.hpE; this.maxHpE=o.maxHpE; this.atkE=o.atkE; this.defE=o.defE; this.spdE=o.spdE; this.wisE=o.wisE;
        this.entAtkE=o.entAtkE; this.entDefE=o.entDefE; this.entSpdE=o.entSpdE; this.entWisE=o.entWisE; this.entHpE=o.entHpE;
        this.lazoE=o.lazoE; this.disciplinaE=o.disciplinaE; this.victoriasE=o.victoriasE; this.derrotasE=o.derrotasE;
        this.vivoE=o.vivoE; this.dormidoE=o.dormidoE; this.dormidoEnSegE=o.dormidoEnSegE; this.vecesLlenoE=o.vecesLlenoE; this.hambreE=o.hambreE; this.energiaE=o.energiaE; this.abiE=o.abiE; this.crecimientoE=o.crecimientoE;
        this.levelE=o.levelE; this.experienceE=o.experienceE;

        this.nombreF=o.nombreF; this.especieF=o.especieF; this.elementoF=o.elementoF; this.nivelEvoF=o.nivelEvoF;
        this.hpF=o.hpF; this.maxHpF=o.maxHpF; this.atkF=o.atkF; this.defF=o.defF; this.spdF=o.spdF; this.wisF=o.wisF;
        this.entAtkF=o.entAtkF; this.entDefF=o.entDefF; this.entSpdF=o.entSpdF; this.entWisF=o.entWisF; this.entHpF=o.entHpF;
        this.lazoF=o.lazoF; this.disciplinaF=o.disciplinaF; this.victoriasF=o.victoriasF; this.derrotasF=o.derrotasF;
        this.vivoF=o.vivoF; this.dormidoF=o.dormidoF; this.dormidoEnSegF=o.dormidoEnSegF; this.vecesLlenoF=o.vecesLlenoF; this.hambreF=o.hambreF; this.energiaF=o.energiaF; this.abiF=o.abiF; this.crecimientoF=o.crecimientoF;
        this.levelF=o.levelF; this.experienceF=o.experienceF;

        this.tieneEquipo=o.tieneEquipo;
        this.recetasDesbloqueadas=o.recetasDesbloqueadas;
        this.incubaHuevoId=o.incubaHuevoId; this.incubaInicioSeg=o.incubaInicioSeg;
        this.incubaCiclos=o.incubaCiclos; this.incubaEnergia=o.incubaEnergia;
        this.incubaNotificado=o.incubaNotificado;
        this.incubaEspecie=o.incubaEspecie; this.incubaExitoPct=o.incubaExitoPct;
        this.incubacionesMesa=o.incubacionesMesa;
        this.bancoDigimon=o.bancoDigimon;
    }

    @Override
    public Component<EntityStore> clone() { return new PetProgressComponent(this); }

    // ═════════════════════════════════════════════════════════════
    // Guardar desde DatosJugador → componente (llamar al desconectar o cambiar datos)
    // ═════════════════════════════════════════════════════════════
    public void guardarDesde(com.digitale.datos.AlmacenJugadores.DatosJugador datos) {
        this.tieneEquipo = datos.tieneEquipo;
        this.recetasDesbloqueadas = String.join(",", datos.recetas);
        this.incubaHuevoId = datos.incubaHuevoId != null ? datos.incubaHuevoId : "";
        this.incubaInicioSeg = datos.incubaInicioSeg;
        this.incubaCiclos = datos.incubaCiclos;
        this.incubaEnergia = datos.incubaEnergia;
        this.incubaNotificado = datos.incubaNotificado;
        this.incubaEspecie = datos.incubaEspecie != null ? datos.incubaEspecie : "";
        this.incubaExitoPct = datos.incubaExitoPct;
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, com.digitale.datos.AlmacenJugadores.DatosJugador.IncubacionMesa> en
                : datos.incubaciones.entrySet()) {
            com.digitale.datos.AlmacenJugadores.DatosJugador.IncubacionMesa im = en.getValue();
            if (im == null || im.huevoId == null || im.huevoId.isEmpty()) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(en.getKey()).append('|').append(im.huevoId).append('|')
                .append(im.especie != null ? im.especie : "").append('|').append(im.exitoPct).append('|')
                .append(im.inicioSeg).append('|').append(im.ciclos).append('|').append(im.segCiclo).append('|')
                .append(im.energia).append('|')
                .append(im.notificado ? 1 : 0).append('|').append(im.sobrealim);
        }
        this.incubacionesMesa = sb.toString();
        try {
            this.bancoDigimon = new com.google.gson.Gson().toJson(datos.banco);
        } catch (Exception e) {
            this.bancoDigimon = "[]";
        }
        com.digitale.datos.DatoDigimon a = datos.getSlot(0);
        if (a != null) {
            nombreA=a.nombre; especieA=a.especie; elementoA=a.elemento; nivelEvoA=a.nivel;
            hpA=a.hp; maxHpA=a.maxHp; atkA=a.atk; defA=a.def; spdA=a.spd; wisA=a.wis;
            entAtkA=a.entAtk; entDefA=a.entDef; entSpdA=a.entSpd; entWisA=a.entWis; entHpA=a.entHp;
            lazoA=a.lazo; disciplinaA=a.disciplina; victoriasA=a.victorias; derrotasA=a.derrotas;
            vivoA=a.vivo; dormidoA=a.dormido; dormidoEnSegA=a.dormidoEnSeg; vecesLlenoA=a.vecesLleno; hambreA=a.hambre; energiaA=a.energia; abiA=a.abi; crecimientoA=a.crecimiento;
            levelA=Math.max(1, a.nivelAv); experienceA=Math.max(0, a.xp);
        } else {
            especieA = ""; nombreA = "";
        }
        com.digitale.datos.DatoDigimon b = datos.getSlot(1);
        if (b != null) {
            nombreB=b.nombre; especieB=b.especie; elementoB=b.elemento; nivelEvoB=b.nivel;
            hpB=b.hp; maxHpB=b.maxHp; atkB=b.atk; defB=b.def; spdB=b.spd; wisB=b.wis;
            entAtkB=b.entAtk; entDefB=b.entDef; entSpdB=b.entSpd; entWisB=b.entWis; entHpB=b.entHp;
            lazoB=b.lazo; disciplinaB=b.disciplina; victoriasB=b.victorias; derrotasB=b.derrotas;
            vivoB=b.vivo; dormidoB=b.dormido; dormidoEnSegB=b.dormidoEnSeg; vecesLlenoB=b.vecesLleno; hambreB=b.hambre; energiaB=b.energia; abiB=b.abi; crecimientoB=b.crecimiento;
            levelB=Math.max(1, b.nivelAv); experienceB=Math.max(0, b.xp);
        } else {
            especieB = ""; nombreB = "";
        }
        guardarSlot(2, datos.getSlot(2));
        guardarSlot(3, datos.getSlot(3));
        guardarSlot(4, datos.getSlot(4));
        guardarSlot(5, datos.getSlot(5));
    }

    private void guardarSlot(int idx, com.digitale.datos.DatoDigimon d) {
        if (d == null) {
            setEspecieSlot(idx, "");
            return;
        }
        setNombreSlot(idx, d.nombre); setEspecieSlot(idx, d.especie);
        setElementoSlot(idx, d.elemento); setNivelEvoSlot(idx, d.nivel);
        setHpSlot(idx, d.hp); setMaxHpSlot(idx, d.maxHp);
        setAtkSlot(idx, d.atk); setDefSlot(idx, d.def);
        setSpdSlot(idx, d.spd); setWisSlot(idx, d.wis);
        setEntAtkSlot(idx, d.entAtk); setEntDefSlot(idx, d.entDef);
        setEntSpdSlot(idx, d.entSpd); setEntWisSlot(idx, d.entWis); setEntHpSlot(idx, d.entHp);
        setLazoSlot(idx, d.lazo); setDisciplinaSlot(idx, d.disciplina);
        setVictoriasSlot(idx, d.victorias); setDerrotasSlot(idx, d.derrotas);
        setVivoSlot(idx, d.vivo); setDormidoSlot(idx, d.dormido); setDormidoEnSegSlot(idx, d.dormidoEnSeg); setVecesLlenoSlot(idx, d.vecesLleno); setHambreSlot(idx, d.hambre);
        setEnergiaSlot(idx, d.energia); setAbiSlot(idx, d.abi); setCrecimientoSlot(idx, d.crecimiento);
        // Progresión de aventura (antes se perdía al guardar los slots C-F).
        setLevelSlot(idx, Math.max(1, d.nivelAv));
        setExperienceSlot(idx, Math.max(0, d.xp));
    }

    // ── Acceso por índice a los slots C-F (2-5) ───────────────────
    private void setNombreSlot(int i, String v) {
        switch (i) { case 2 -> nombreC = v; case 3 -> nombreD = v; case 4 -> nombreE = v; default -> nombreF = v; }
    }
    private void setEspecieSlot(int i, String v) {
        switch (i) { case 2 -> especieC = v; case 3 -> especieD = v; case 4 -> especieE = v; default -> especieF = v; }
    }
    private void setElementoSlot(int i, String v) {
        switch (i) { case 2 -> elementoC = v; case 3 -> elementoD = v; case 4 -> elementoE = v; default -> elementoF = v; }
    }
    private void setNivelEvoSlot(int i, int v) {
        switch (i) { case 2 -> nivelEvoC = v; case 3 -> nivelEvoD = v; case 4 -> nivelEvoE = v; default -> nivelEvoF = v; }
    }
    private void setHpSlot(int i, int v) {
        switch (i) { case 2 -> hpC = v; case 3 -> hpD = v; case 4 -> hpE = v; default -> hpF = v; }
    }
    private void setMaxHpSlot(int i, int v) {
        switch (i) { case 2 -> maxHpC = v; case 3 -> maxHpD = v; case 4 -> maxHpE = v; default -> maxHpF = v; }
    }
    private void setAtkSlot(int i, int v) {
        switch (i) { case 2 -> atkC = v; case 3 -> atkD = v; case 4 -> atkE = v; default -> atkF = v; }
    }
    private void setDefSlot(int i, int v) {
        switch (i) { case 2 -> defC = v; case 3 -> defD = v; case 4 -> defE = v; default -> defF = v; }
    }
    private void setSpdSlot(int i, int v) {
        switch (i) { case 2 -> spdC = v; case 3 -> spdD = v; case 4 -> spdE = v; default -> spdF = v; }
    }
    private void setWisSlot(int i, int v) {
        switch (i) { case 2 -> wisC = v; case 3 -> wisD = v; case 4 -> wisE = v; default -> wisF = v; }
    }
    private void setEntAtkSlot(int i, int v) {
        switch (i) { case 2 -> entAtkC = v; case 3 -> entAtkD = v; case 4 -> entAtkE = v; default -> entAtkF = v; }
    }
    private void setEntDefSlot(int i, int v) {
        switch (i) { case 2 -> entDefC = v; case 3 -> entDefD = v; case 4 -> entDefE = v; default -> entDefF = v; }
    }
    private void setEntSpdSlot(int i, int v) {
        switch (i) { case 2 -> entSpdC = v; case 3 -> entSpdD = v; case 4 -> entSpdE = v; default -> entSpdF = v; }
    }
    private void setEntWisSlot(int i, int v) {
        switch (i) { case 2 -> entWisC = v; case 3 -> entWisD = v; case 4 -> entWisE = v; default -> entWisF = v; }
    }
    private void setEntHpSlot(int i, int v) {
        switch (i) { case 2 -> entHpC = v; case 3 -> entHpD = v; case 4 -> entHpE = v; default -> entHpF = v; }
    }
    private void setLazoSlot(int i, int v) {
        switch (i) { case 2 -> lazoC = v; case 3 -> lazoD = v; case 4 -> lazoE = v; default -> lazoF = v; }
    }
    private void setDisciplinaSlot(int i, int v) {
        switch (i) { case 2 -> disciplinaC = v; case 3 -> disciplinaD = v; case 4 -> disciplinaE = v; default -> disciplinaF = v; }
    }
    private void setVictoriasSlot(int i, int v) {
        switch (i) { case 2 -> victoriasC = v; case 3 -> victoriasD = v; case 4 -> victoriasE = v; default -> victoriasF = v; }
    }
    private void setDerrotasSlot(int i, int v) {
        switch (i) { case 2 -> derrotasC = v; case 3 -> derrotasD = v; case 4 -> derrotasE = v; default -> derrotasF = v; }
    }
    private void setVivoSlot(int i, boolean v) {
        switch (i) { case 2 -> vivoC = v; case 3 -> vivoD = v; case 4 -> vivoE = v; default -> vivoF = v; }
    }
    private void setDormidoSlot(int i, boolean v) {
        switch (i) { case 2 -> dormidoC = v; case 3 -> dormidoD = v; case 4 -> dormidoE = v; default -> dormidoF = v; }
    }
    private void setDormidoEnSegSlot(int i, int v) {
        switch (i) { case 2 -> dormidoEnSegC = v; case 3 -> dormidoEnSegD = v; case 4 -> dormidoEnSegE = v; default -> dormidoEnSegF = v; }
    }
    private void setVecesLlenoSlot(int i, int v) {
        switch (i) { case 2 -> vecesLlenoC = v; case 3 -> vecesLlenoD = v; case 4 -> vecesLlenoE = v; default -> vecesLlenoF = v; }
    }
    private void setHambreSlot(int i, int v) {
        switch (i) { case 2 -> hambreC = v; case 3 -> hambreD = v; case 4 -> hambreE = v; default -> hambreF = v; }
    }
    private void setEnergiaSlot(int i, int v) {
        switch (i) { case 2 -> energiaC = v; case 3 -> energiaD = v; case 4 -> energiaE = v; default -> energiaF = v; }
    }
    private void setAbiSlot(int i, int v) {
        switch (i) { case 2 -> abiC = v; case 3 -> abiD = v; case 4 -> abiE = v; default -> abiF = v; }
    }
    private void setCrecimientoSlot(int i, double v) {
        switch (i) { case 2 -> crecimientoC = v; case 3 -> crecimientoD = v; case 4 -> crecimientoE = v; default -> crecimientoF = v; }
    }
    private void setLevelSlot(int i, int v) {
        switch (i) { case 2 -> levelC = v; case 3 -> levelD = v; case 4 -> levelE = v; default -> levelF = v; }
    }
    private void setExperienceSlot(int i, int v) {
        switch (i) { case 2 -> experienceC = v; case 3 -> experienceD = v; case 4 -> experienceE = v; default -> experienceF = v; }
    }

    // ═════════════════════════════════════════════════════════════
    // Cargar de componente → DatosJugador (llamar al conectar)
    // ═════════════════════════════════════════════════════════════
    public void cargarEn(com.digitale.datos.AlmacenJugadores.DatosJugador datos) {
        // Recetas siempre (aunque aún no tenga equipo).
        datos.recetas.clear();
        if (recetasDesbloqueadas != null && !recetasDesbloqueadas.isEmpty()) {
            for (String r : recetasDesbloqueadas.split(",")) {
                if (!r.isEmpty()) datos.recetas.add(r);
            }
        }
        // Incubación siempre (aunque aún no tenga equipo).
        datos.incubaHuevoId = incubaHuevoId != null ? incubaHuevoId : "";
        datos.incubaInicioSeg = incubaInicioSeg;
        datos.incubaCiclos = incubaCiclos;
        datos.incubaEnergia = incubaEnergia != 0 ? incubaEnergia : 5;
        datos.incubaNotificado = incubaNotificado;
        datos.incubaEspecie = incubaEspecie != null ? incubaEspecie : "";
        datos.incubaExitoPct = incubaExitoPct != 0 ? incubaExitoPct : 100;
        datos.banco.clear();
        if (bancoDigimon != null && !bancoDigimon.isEmpty()) {
            try {
                java.lang.reflect.Type tipo = new com.google.gson.reflect.TypeToken<java.util.List<com.digitale.datos.DatoDigimon>>() {
                }.getType();
                java.util.List<com.digitale.datos.DatoDigimon> lista =
                    new com.google.gson.Gson().fromJson(bancoDigimon, tipo);
                if (lista != null) {
                    for (com.digitale.datos.DatoDigimon b : lista) {
                        if (b != null && b.especie != null && !b.especie.isEmpty()
                            && datos.banco.size() < com.digitale.datos.AlmacenJugadores.DatosJugador.BANCO_MAX) {
                            b.repararIntegridad();
                            datos.banco.add(b);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        datos.incubaciones.clear();
        if (incubacionesMesa != null && !incubacionesMesa.isEmpty()) {
            for (String e : incubacionesMesa.split(";")) {
                try {
                    String[] p = e.split("\\|", -1);
                    if (p.length < 9 || p[1].isEmpty()) continue;
                    com.digitale.datos.AlmacenJugadores.DatosJugador.IncubacionMesa im =
                        new com.digitale.datos.AlmacenJugadores.DatosJugador.IncubacionMesa();
                    im.huevoId = p[1];
                    im.especie = p[2];
                    im.exitoPct = Integer.parseInt(p[3]);
                    im.inicioSeg = Integer.parseInt(p[4]);
                    im.ciclos = Integer.parseInt(p[5]);
                    int idx = 6;
                    if (p.length >= 10) {
                        try {
                            im.segCiclo = Math.max(1, Integer.parseInt(p[6]));
                        } catch (Exception ignored) {
                            im.segCiclo = 60;
                        }
                        idx = 7;
                    }
                    im.energia = Integer.parseInt(p[idx]);
                    im.notificado = "1".equals(p[idx + 1]);
                    im.sobrealim = Integer.parseInt(p[idx + 2]);
                    datos.incubaciones.put(p[0], im);
                } catch (Exception ignored) {
                }
            }
        }
        // Migración legado: incubación global única -> clave "clasica".
        if (datos.incubaciones.isEmpty() && incubaHuevoId != null && !incubaHuevoId.isEmpty()) {
            com.digitale.datos.AlmacenJugadores.DatosJugador.IncubacionMesa im =
                new com.digitale.datos.AlmacenJugadores.DatosJugador.IncubacionMesa();
            im.huevoId = incubaHuevoId;
            im.especie = incubaEspecie != null ? incubaEspecie : "";
            im.exitoPct = incubaExitoPct != 0 ? incubaExitoPct : 100;
            im.inicioSeg = incubaInicioSeg;
            im.ciclos = incubaCiclos;
            im.energia = incubaEnergia != 0 ? incubaEnergia : 5;
            im.notificado = incubaNotificado;
            datos.incubaciones.put("clasica", im);
        }
        if (!tieneEquipo) return;

        datos.tieneEquipo = true;
        boolean alguno = false;        for (int i = 0; i < 6; i++) {
            com.digitale.datos.DatoDigimon d = leerSlot(i);
            datos.setSlot(i, d);
            if (d != null) alguno = true;
        }
        if (!alguno) {
            datos.tieneEquipo = false;
            return;
        }
        datos.activoA = 0;
        datos.activoB = 1;
    }

    private com.digitale.datos.DatoDigimon leerSlot(int idx) {
        String especie = switch (idx) {
            case 0 -> especieA; case 1 -> especieB; case 2 -> especieC;
            case 3 -> especieD; case 4 -> especieE; default -> especieF;
        };
        if (especie == null || especie.isEmpty()) return null;

        com.digitale.datos.DatoDigimon d = new com.digitale.datos.DatoDigimon();
        d.nombre = pick(idx, nombreA, nombreB, nombreC, nombreD, nombreE, nombreF);
        d.especie = especie;
        d.elemento = pick(idx, elementoA, elementoB, elementoC, elementoD, elementoE, elementoF);
        d.nivel = pickInt(idx, nivelEvoA, nivelEvoB, nivelEvoC, nivelEvoD, nivelEvoE, nivelEvoF);
        d.hp = pickInt(idx, hpA, hpB, hpC, hpD, hpE, hpF);
        d.maxHp = pickInt(idx, maxHpA, maxHpB, maxHpC, maxHpD, maxHpE, maxHpF);
        d.atk = pickInt(idx, atkA, atkB, atkC, atkD, atkE, atkF);
        d.def = pickInt(idx, defA, defB, defC, defD, defE, defF);
        d.spd = pickInt(idx, spdA, spdB, spdC, spdD, spdE, spdF);
        d.wis = pickInt(idx, wisA, wisB, wisC, wisD, wisE, wisF);
        d.entAtk = pickInt(idx, entAtkA, entAtkB, entAtkC, entAtkD, entAtkE, entAtkF);
        d.entDef = pickInt(idx, entDefA, entDefB, entDefC, entDefD, entDefE, entDefF);
        d.entSpd = pickInt(idx, entSpdA, entSpdB, entSpdC, entSpdD, entSpdE, entSpdF);
        d.entWis = pickInt(idx, entWisA, entWisB, entWisC, entWisD, entWisE, entWisF);
        d.entHp = pickInt(idx, entHpA, entHpB, entHpC, entHpD, entHpE, entHpF);
        d.lazo = pickInt(idx, lazoA, lazoB, lazoC, lazoD, lazoE, lazoF);
        d.disciplina = pickInt(idx, disciplinaA, disciplinaB, disciplinaC, disciplinaD, disciplinaE, disciplinaF);
        d.victorias = pickInt(idx, victoriasA, victoriasB, victoriasC, victoriasD, victoriasE, victoriasF);
        d.derrotas = pickInt(idx, derrotasA, derrotasB, derrotasC, derrotasD, derrotasE, derrotasF);
        d.vivo = pickBool(idx, vivoA, vivoB, vivoC, vivoD, vivoE, vivoF);
        d.dormido = pickBool(idx, dormidoA, dormidoB, dormidoC, dormidoD, dormidoE, dormidoF);
        d.dormidoEnSeg = pickInt(idx, dormidoEnSegA, dormidoEnSegB, dormidoEnSegC, dormidoEnSegD, dormidoEnSegE, dormidoEnSegF);
        d.vecesLleno = pickInt(idx, vecesLlenoA, vecesLlenoB, vecesLlenoC, vecesLlenoD, vecesLlenoE, vecesLlenoF);
        d.nivelAv = pickInt(idx, levelA, levelB, levelC, levelD, levelE, levelF);
        if (d.nivelAv < 1) d.nivelAv = 1;
        d.xp = pickInt(idx, experienceA, experienceB, experienceC, experienceD, experienceE, experienceF);
        d.hambre = pickInt(idx, hambreA, hambreB, hambreC, hambreD, hambreE, hambreF);
        d.energia = pickInt(idx, energiaA, energiaB, energiaC, energiaD, energiaE, energiaF);
        d.abi = pickInt(idx, abiA, abiB, abiC, abiD, abiE, abiF);
        d.crecimiento = pickDouble(idx, crecimientoA, crecimientoB, crecimientoC, crecimientoD, crecimientoE, crecimientoF);
        // Repara atributo/HP/XP de partidas antiguas antes de usarlo.
        d.repararIntegridad();
        d.hpCombate = d.hp;
        return d;
    }

    private static String pick(int idx, String a, String b, String c, String d, String e, String f) {
        return switch (idx) { case 0 -> a; case 1 -> b; case 2 -> c; case 3 -> d; case 4 -> e; default -> f; };
    }

    private static int pickInt(int idx, int a, int b, int c, int d, int e, int f) {
        return switch (idx) { case 0 -> a; case 1 -> b; case 2 -> c; case 3 -> d; case 4 -> e; default -> f; };
    }

    private static boolean pickBool(int idx, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f) {
        return switch (idx) { case 0 -> a; case 1 -> b; case 2 -> c; case 3 -> d; case 4 -> e; default -> f; };
    }

    private static double pickDouble(int idx, double a, double b, double c, double d, double e, double f) {
        return switch (idx) { case 0 -> a; case 1 -> b; case 2 -> c; case 3 -> d; case 4 -> e; default -> f; };
    }

    // ═════════════════════════════════════════════════════════════
    // Getters / Setters de progreso (nivel XP de aventura)
    // ═════════════════════════════════════════════════════════════
    public int getLevelA()      { return levelA; }
    public int getExperienceA() { return experienceA; }
    public int getLevelB()      { return levelB; }
    public int getExperienceB() { return experienceB; }

    public void setLevelA(int v)      { levelA = Math.max(1, v); }
    public void setExperienceA(int v) { experienceA = Math.max(0, v); }
    public void setLevelB(int v)      { levelB = Math.max(1, v); }
    public void setExperienceB(int v) { experienceB = Math.max(0, v); }

    public boolean addExperienceA(int exp) {
        int old = levelA; experienceA += exp;
        while (experienceA >= 100) { levelA++; experienceA -= 100; }
        return levelA > old;
    }

    public boolean addExperienceB(int exp) {
        int old = levelB; experienceB += exp;
        while (experienceB >= 100) { levelB++; experienceB -= 100; }
        return levelB > old;
    }

    public void resetA() { levelA=1; experienceA=0; }
    public void resetB() { levelB=1; experienceB=0; }
    public void resetAll() {
        resetA(); resetB();
        levelC=1; experienceC=0; levelD=1; experienceD=0;
        levelE=1; experienceE=0; levelF=1; experienceF=0;
    }

    /** Resetea el slot i (0-5). Retorna false si el índice es inválido. */
    public boolean resetSlot(int i) {
        switch (i) {
            case 0 -> { levelA=1; experienceA=0; return true; }
            case 1 -> { levelB=1; experienceB=0; return true; }
            case 2 -> { levelC=1; experienceC=0; return true; }
            case 3 -> { levelD=1; experienceD=0; return true; }
            case 4 -> { levelE=1; experienceE=0; return true; }
            case 5 -> { levelF=1; experienceF=0; return true; }
            default -> { return false; }
        }
    }

    /** Setea nivel/XP del slot i (0-5). Retorna false si el índice es inválido. */
    public boolean setSlotProgress(int i, int level, int exp) {
        int lvl = Math.max(1, level);
        int xp = Math.max(0, exp);
        switch (i) {
            case 0 -> { levelA=lvl; experienceA=xp; return true; }
            case 1 -> { levelB=lvl; experienceB=xp; return true; }
            case 2 -> { levelC=lvl; experienceC=xp; return true; }
            case 3 -> { levelD=lvl; experienceD=xp; return true; }
            case 4 -> { levelE=lvl; experienceE=xp; return true; }
            case 5 -> { levelF=lvl; experienceF=xp; return true; }
            default -> { return false; }
        }
    }

    /** Convierte "a"-"f" o "1"-"6" a índice de slot (0-5), o -1 si inválido. */
    public static int slotDesdeNombre(String s) {
        if (s == null) return -1;
        return switch (s.toLowerCase()) {
            case "a", "1" -> 0;
            case "b", "2" -> 1;
            case "c", "3" -> 2;
            case "d", "4" -> 3;
            case "e", "5" -> 4;
            case "f", "6" -> 5;
            default -> -1;
        };
    }

    public boolean isTieneEquipo() { return tieneEquipo; }

    public String getProgressString() {
        return String.format("A: Lvl %d (%d/100) | B: Lvl %d (%d/100) | C: Lvl %d (%d/100) | D: Lvl %d (%d/100) | E: Lvl %d (%d/100) | F: Lvl %d (%d/100)",
                levelA, experienceA, levelB, experienceB, levelC, experienceC,
                levelD, experienceD, levelE, experienceE, levelF, experienceF);
    }
}