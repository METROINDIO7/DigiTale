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
    private int hambreA     = 100; private int energiaA   = 100;
    private int abiA        = 0;

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
    private int hambreB     = 100; private int energiaB   = 100;
    private int abiB        = 0;

    // ── Compañero B: progreso (XP/nivel de aventura) ─────────────
    private int levelB      = 1;
    private int experienceB = 0;

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
            .append(new KeyedCodec<>("HambreA",     Codec.INTEGER), (d,v)->d.hambreA=v,     d->d.hambreA).add()
            .append(new KeyedCodec<>("EnergiaA",    Codec.INTEGER), (d,v)->d.energiaA=v,    d->d.energiaA).add()
            .append(new KeyedCodec<>("AbiA",        Codec.INTEGER), (d,v)->d.abiA=v,        d->d.abiA).add()
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
            .append(new KeyedCodec<>("HambreB",     Codec.INTEGER), (d,v)->d.hambreB=v,     d->d.hambreB).add()
            .append(new KeyedCodec<>("EnergiaB",    Codec.INTEGER), (d,v)->d.energiaB=v,    d->d.energiaB).add()
            .append(new KeyedCodec<>("AbiB",        Codec.INTEGER), (d,v)->d.abiB=v,        d->d.abiB).add()
            // ── B progreso
            .append(new KeyedCodec<>("LevelB",      Codec.INTEGER), (d,v)->d.levelB=v,      d->d.levelB).add()
            .append(new KeyedCodec<>("ExperienceB", Codec.INTEGER), (d,v)->d.experienceB=v, d->d.experienceB).add()
            // ── flags
            .append(new KeyedCodec<>("TieneEquipo", Codec.BOOLEAN), (d,v)->d.tieneEquipo=v, d->d.tieneEquipo).add()
            .build();

    public PetProgressComponent() {}

    public PetProgressComponent(PetProgressComponent o) {
        this.nombreA=o.nombreA; this.especieA=o.especieA; this.elementoA=o.elementoA; this.nivelEvoA=o.nivelEvoA;
        this.hpA=o.hpA; this.maxHpA=o.maxHpA; this.atkA=o.atkA; this.defA=o.defA; this.spdA=o.spdA; this.wisA=o.wisA;
        this.entAtkA=o.entAtkA; this.entDefA=o.entDefA; this.entSpdA=o.entSpdA; this.entWisA=o.entWisA; this.entHpA=o.entHpA;
        this.lazoA=o.lazoA; this.disciplinaA=o.disciplinaA; this.victoriasA=o.victoriasA; this.derrotasA=o.derrotasA;
        this.vivoA=o.vivoA; this.hambreA=o.hambreA; this.energiaA=o.energiaA; this.abiA=o.abiA;
        this.levelA=o.levelA; this.experienceA=o.experienceA;

        this.nombreB=o.nombreB; this.especieB=o.especieB; this.elementoB=o.elementoB; this.nivelEvoB=o.nivelEvoB;
        this.hpB=o.hpB; this.maxHpB=o.maxHpB; this.atkB=o.atkB; this.defB=o.defB; this.spdB=o.spdB; this.wisB=o.wisB;
        this.entAtkB=o.entAtkB; this.entDefB=o.entDefB; this.entSpdB=o.entSpdB; this.entWisB=o.entWisB; this.entHpB=o.entHpB;
        this.lazoB=o.lazoB; this.disciplinaB=o.disciplinaB; this.victoriasB=o.victoriasB; this.derrotasB=o.derrotasB;
        this.vivoB=o.vivoB; this.hambreB=o.hambreB; this.energiaB=o.energiaB; this.abiB=o.abiB;
        this.levelB=o.levelB; this.experienceB=o.experienceB;

        this.tieneEquipo=o.tieneEquipo;
    }

    @Override
    public Component<EntityStore> clone() { return new PetProgressComponent(this); }

    // ═════════════════════════════════════════════════════════════
    // Guardar desde DatosJugador → componente (llamar al desconectar o cambiar datos)
    // ═════════════════════════════════════════════════════════════
    public void guardarDesde(com.digitale.datos.AlmacenJugadores.DatosJugador datos) {
        this.tieneEquipo = datos.tieneEquipo;
        if (datos.companeroA != null) {
            com.digitale.datos.DatoDigimon a = datos.companeroA;
            nombreA=a.nombre; especieA=a.especie; elementoA=a.elemento; nivelEvoA=a.nivel;
            hpA=a.hp; maxHpA=a.maxHp; atkA=a.atk; defA=a.def; spdA=a.spd; wisA=a.wis;
            entAtkA=a.entAtk; entDefA=a.entDef; entSpdA=a.entSpd; entWisA=a.entWis; entHpA=a.entHp;
            lazoA=a.lazo; disciplinaA=a.disciplina; victoriasA=a.victorias; derrotasA=a.derrotas;
            vivoA=a.vivo; hambreA=a.hambre; energiaA=a.energia; abiA=a.abi;
        }
        if (datos.companeroB != null) {
            com.digitale.datos.DatoDigimon b = datos.companeroB;
            nombreB=b.nombre; especieB=b.especie; elementoB=b.elemento; nivelEvoB=b.nivel;
            hpB=b.hp; maxHpB=b.maxHp; atkB=b.atk; defB=b.def; spdB=b.spd; wisB=b.wis;
            entAtkB=b.entAtk; entDefB=b.entDef; entSpdB=b.entSpd; entWisB=b.entWis; entHpB=b.entHp;
            lazoB=b.lazo; disciplinaB=b.disciplina; victoriasB=b.victorias; derrotasB=b.derrotas;
            vivoB=b.vivo; hambreB=b.hambre; energiaB=b.energia; abiB=b.abi;
        }
    }

    // ═════════════════════════════════════════════════════════════
    // Cargar de componente → DatosJugador (llamar al conectar)
    // ═════════════════════════════════════════════════════════════
    public void cargarEn(com.digitale.datos.AlmacenJugadores.DatosJugador datos) {
        if (!tieneEquipo || especieA.isEmpty()) return;

        datos.tieneEquipo = true;

        com.digitale.datos.DatoDigimon a = new com.digitale.datos.DatoDigimon();
        a.nombre=nombreA; a.especie=especieA; a.elemento=elementoA; a.nivel=nivelEvoA;
        a.hp=hpA; a.maxHp=maxHpA; a.atk=atkA; a.def=defA; a.spd=spdA; a.wis=wisA;
        a.entAtk=entAtkA; a.entDef=entDefA; a.entSpd=entSpdA; a.entWis=entWisA; a.entHp=entHpA;
        a.lazo=lazoA; a.disciplina=disciplinaA; a.victorias=victoriasA; a.derrotas=derrotasA;
        a.vivo=vivoA; a.hambre=hambreA; a.energia=energiaA; a.abi=abiA;
        a.hpCombate=hpA;
        datos.companeroA = a;

        if (!especieB.isEmpty()) {
            com.digitale.datos.DatoDigimon b = new com.digitale.datos.DatoDigimon();
            b.nombre=nombreB; b.especie=especieB; b.elemento=elementoB; b.nivel=nivelEvoB;
            b.hp=hpB; b.maxHp=maxHpB; b.atk=atkB; b.def=defB; b.spd=spdB; b.wis=wisB;
            b.entAtk=entAtkB; b.entDef=entDefB; b.entSpd=entSpdB; b.entWis=entWisB; b.entHp=entHpB;
            b.lazo=lazoB; b.disciplina=disciplinaB; b.victorias=victoriasB; b.derrotas=derrotasB;
            b.vivo=vivoB; b.hambre=hambreB; b.energia=energiaB; b.abi=abiB;
            b.hpCombate=hpB;
            datos.companeroB = b;
        }
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
    public void resetAll() { resetA(); resetB(); }

    public boolean isTieneEquipo() { return tieneEquipo; }

    public String getProgressString() {
        return String.format("A: Lvl %d (%d/100 XP) | B: Lvl %d (%d/100 XP)",
                levelA, experienceA, levelB, experienceB);
    }
}