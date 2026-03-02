package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DigiMainMenuUI extends InteractiveCustomUIPage<DigiMainMenuUI.Data> {

    // ── Data ────────────────────────────────────────────────────────
    public static class Data {

        // ¡IMPORTANTE! Los campos deben declararse ANTES del CODEC
        // para que las lambdas puedan resolverlos en tiempo de compilación.
        public String accion              = "";
        public String pestanaActiva       = "estado";
        public String slotEvolucionarActivo = "a";

        // Patrón correcto de BuilderCodec:
        //   .append(codec, setter, getter)   ← devuelve FieldBuilder
        //   .add()                           ← finaliza el campo, vuelve al builder
        //   ...repetir por cada campo...
        //   .build()                         ← construye el codec final
        public static final BuilderCodec<Data> CODEC = BuilderCodec
                .<Data>builder(Data.class, Data::new)
                .append(
                        new KeyedCodec<>("Accion", Codec.STRING),
                        (d, v) -> d.accion = v,
                        d -> d.accion
                ).add()
                .append(
                        new KeyedCodec<>("PestanaActiva", Codec.STRING),
                        (d, v) -> d.pestanaActiva = v,
                        d -> d.pestanaActiva
                ).add()
                .append(
                        new KeyedCodec<>("SlotEvolucionar", Codec.STRING),
                        (d, v) -> d.slotEvolucionarActivo = v,
                        d -> d.slotEvolucionarActivo
                ).add()
                .build();
    }

    // ── Campos de instancia ─────────────────────────────────────────
    private final PlayerRef playerRef;
    private final String[] evosActuales = {"", "", ""};

    public DigiMainMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    // ── build ───────────────────────────────────────────────────────
    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("Pages/DigiMainMenu.ui");

        // Pestanas
        DigiUIHelper.bindClick(eventBuilder, "#BtnTabEstado",      "Accion", "tab_estado");
        DigiUIHelper.bindClick(eventBuilder, "#BtnTabEvolucionar", "Accion", "tab_evolucionar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnTabEntrenar",    "Accion", "tab_entrenar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnTabCuidar",      "Accion", "tab_cuidar");

        // Evolucionar
        DigiUIHelper.bindClick(eventBuilder, "#EvoTabA",  "Accion", "evo_tab_a");
        DigiUIHelper.bindClick(eventBuilder, "#EvoTabB",  "Accion", "evo_tab_b");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvo1",  "Accion", "evo_0");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvo2",  "Accion", "evo_1");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvo3",  "Accion", "evo_2");

        // Entrenar
        for (String stat : new String[]{"atk", "def", "spd", "wis", "hp"}) {
            DigiUIHelper.bindClick(eventBuilder, "#Btn" + capitalizar(stat), "Accion", "stat_" + stat);
        }

        // Cuidar
        DigiUIHelper.bindClick(eventBuilder, "#BtnAlimentarA",  "Accion", "alimentar_a");
        DigiUIHelper.bindClick(eventBuilder, "#BtnAlimentarB",  "Accion", "alimentar_b");
        DigiUIHelper.bindClick(eventBuilder, "#BtnMimarA",      "Accion", "mimar_a");
        DigiUIHelper.bindClick(eventBuilder, "#BtnMimarB",      "Accion", "mimar_b");
        DigiUIHelper.bindClick(eventBuilder, "#BtnDescansarAB", "Accion", "descansar_ab");

        // Cerrar
        DigiUIHelper.bindClick(eventBuilder, "#BtnCerrar", "Accion", "cerrar");

        // Mostrar pestaña inicial (estado)
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        UICommandBuilder b = new UICommandBuilder();
        actualizarVisibilidadPestanas(b, datos, "estado");
        sendUpdate(b, null, false);
    }

    // ── handleDataEvent ─────────────────────────────────────────────
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { sendUpdate(new UICommandBuilder(), null, false); return; }

        UICommandBuilder b = new UICommandBuilder();
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());

        // Cambio de pestana
        if (data.accion.startsWith("tab_")) {
            data.pestanaActiva = data.accion.substring(4);
            actualizarVisibilidadPestanas(b, datos, data.pestanaActiva);
            sendUpdate(b, null, false);
            return;
        }

        // Cerrar
        if ("cerrar".equals(data.accion)) {
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        // Evolucionar — cambio de slot
        if ("evo_tab_a".equals(data.accion)) {
            data.slotEvolucionarActivo = "a";
            rellenarEvolucionTab(b, datos, data);
            sendUpdate(b, null, false);
            return;
        }
        if ("evo_tab_b".equals(data.accion)) {
            data.slotEvolucionarActivo = "b";
            rellenarEvolucionTab(b, datos, data);
            sendUpdate(b, null, false);
            return;
        }

        // Evolucionar — ejecutar
        if (data.accion.startsWith("evo_")) {
            int idx = Integer.parseInt(data.accion.substring(4));
            intentarEvolucionar(idx, datos, b, data);
            sendUpdate(b, null, false);
            return;
        }

        // Entrenar
        if (data.accion.startsWith("stat_")) {
            String stat = data.accion.substring(5);
            StringBuilder msg = new StringBuilder();
            if (datos != null && datos.companeroA != null && datos.companeroA.vivo) {
                boolean ok = datos.companeroA.entrenar(stat, 1);
                msg.append(ok ? datos.companeroA.nombre + " entrenó " + stat.toUpperCase() + "  "
                        : datos.companeroA.nombre + " sin energía  ");
            }
            if (datos != null && datos.companeroB != null && datos.companeroB.vivo) {
                boolean ok = datos.companeroB.entrenar(stat, 1);
                msg.append(ok ? datos.companeroB.nombre + " entrenó " + stat.toUpperCase()
                        : datos.companeroB.nombre + " sin energía");
            }
            b.set("#Resultado.Text", msg.toString());
            actualizarEnergias(b, datos);
            sendUpdate(b, null, false);
            return;
        }

        // Cuidar
        if (data.accion.startsWith("alimentar_") || data.accion.startsWith("mimar_") ||
                data.accion.equals("descansar_ab")) {
            if (datos != null) {
                String resultado = aplicarCuidado(data.accion, datos);
                b.set("#MsgCuidado.Text", resultado);
                actualizarEstadosCuidar(b, datos);
            }
            sendUpdate(b, null, false);
            return;
        }

        sendUpdate(new UICommandBuilder(), null, false);
    }

    // ── Helpers de UI ───────────────────────────────────────────────

    private void actualizarVisibilidadPestanas(UICommandBuilder b, DatosJugador datos, String activa) {
        b.set("#TabEstado.Visible",     false);
        b.set("#TabEvolucionar.Visible",false);
        b.set("#TabEntrenar.Visible",   false);
        b.set("#TabCuidar.Visible",     false);

        switch (activa) {
            case "estado"       -> { b.set("#TabEstado.Visible", true);      rellenarEstadoTab(b, datos); }
            case "evolucionar"  -> { b.set("#TabEvolucionar.Visible", true); rellenarEvolucionTab(b, datos, new Data()); }
            case "entrenar"     -> { b.set("#TabEntrenar.Visible", true);    actualizarEnergias(b, datos); }
            case "cuidar"       -> { b.set("#TabCuidar.Visible", true);      if (datos != null) actualizarEstadosCuidar(b, datos); }
        }
    }

    private void rellenarEstadoTab(UICommandBuilder b, DatosJugador datos) {
        if (datos == null) return;
        setDigimon(b, datos.companeroA, "A");
        setDigimon(b, datos.companeroB, "B");
    }

    private void setDigimon(UICommandBuilder b, DatoDigimon d, String slot) {
        if (d == null || !d.vivo) {
            b.set("#Nombre" + slot + ".Text",  "Sin companero");
            b.set("#Especie" + slot + ".Text", "---");
            b.set("#Elemento" + slot + ".Text","---");
            b.set("#Hp" + slot + ".Text",  "HP: ---");
            b.set("#Atk" + slot + ".Text", "ATK: ---");
            b.set("#Def" + slot + ".Text", "DEF: ---");
            b.set("#Spd" + slot + ".Text", "SPD: ---");
            b.set("#Wis" + slot + ".Text", "WIS: ---");
            b.set("#Lazo" + slot + ".Text","Lazo: ---");
            b.set("#Abi" + slot + ".Text", "ABI: ---");
            b.set("#Vic" + slot + ".Text", "Victorias: ---");
            b.set("#Energia" + slot + ".Text", "Energia: ---");
            return;
        }
        b.set("#Nombre" + slot + ".Text",  d.nombre);
        b.set("#Especie" + slot + ".Text", d.especie + " - " + d.nombreNivel());
        b.set("#Elemento" + slot + ".Text",d.elemento);
        b.set("#Hp" + slot + ".Text",  "HP: " + d.hp + "/" + d.maxHp);
        b.set("#Atk" + slot + ".Text", "ATK: " + d.atk);
        b.set("#Def" + slot + ".Text", "DEF: " + d.def);
        b.set("#Spd" + slot + ".Text", "SPD: " + d.spd);
        b.set("#Wis" + slot + ".Text", "WIS: " + d.wis);
        b.set("#Lazo" + slot + ".Text","Lazo: " + d.lazo + "/100");
        b.set("#Abi" + slot + ".Text", "ABI: " + d.abi);
        b.set("#Vic" + slot + ".Text", "Victorias: " + d.victorias);
        b.set("#Energia" + slot + ".Text", "Energia: " + d.energia + "/100");
    }

    private void rellenarEvolucionTab(UICommandBuilder b, DatosJugador datos, Data data) {
        if (datos == null) return;
        DatoDigimon d = "a".equals(data.slotEvolucionarActivo) ? datos.companeroA : datos.companeroB;
        if (d == null || !d.vivo) {
            b.set("#InfoNombre.Text",  "Sin companero");
            b.set("#InfoEspecie.Text", "---");
            b.set("#InfoStats.Text",   "---");
            limpiarBotonesEvo(b);
            return;
        }
        b.set("#InfoNombre.Text",  d.nombre);
        b.set("#InfoEspecie.Text", d.especie + " - " + d.nombreNivel() + " - " + d.elemento);
        b.set("#InfoStats.Text",
                "ATK:" + d.atk + " DEF:" + d.def + " SPD:" + d.spd +
                        " WIS:" + d.wis + " V:" + d.victorias + " Lazo:" + d.lazo);

        String[][] opciones = evolucionesParaEspecie(d.especie);
        String[] btnIds = {"#BtnEvo1", "#BtnEvo2", "#BtnEvo3"};
        for (int i = 0; i < 3; i++) {
            if (i < opciones.length) {
                evosActuales[i] = opciones[i][0];
                b.set(btnIds[i] + ".Text", opciones[i][1]);
            } else {
                evosActuales[i] = "";
                b.set(btnIds[i] + ".Text", "---");
            }
        }
        b.set("#MsgEvolucion.Text", "");
    }

    private void limpiarBotonesEvo(UICommandBuilder b) {
        b.set("#BtnEvo1.Text", "---");
        b.set("#BtnEvo2.Text", "---");
        b.set("#BtnEvo3.Text", "---");
        b.set("#MsgEvolucion.Text", "");
        for (int i = 0; i < 3; i++) evosActuales[i] = "";
    }

    private void intentarEvolucionar(int idx, DatosJugador datos, UICommandBuilder b, Data data) {
        if (idx < 0 || idx >= evosActuales.length || evosActuales[idx].isEmpty()) return;
        DatoDigimon d = "a".equals(data.slotEvolucionarActivo) ? datos.companeroA : datos.companeroB;
        if (d == null || !d.vivo) return;

        String forma = evosActuales[idx];
        if (d.puedeEvolucionar(forma)) {
            DatoDigimon evo = DatoDigimon.crearInicial(forma);
            d.especie  = evo.especie;
            d.elemento = evo.elemento;
            d.maxHp = evo.maxHp;  d.atk = evo.atk;  d.def = evo.def;
            d.spd   = evo.spd;    d.wis = evo.wis;   d.nivel = evo.nivel;
            d.hp = d.maxHp;
            b.set("#MsgEvolucion.Text", d.nombre + " evoluciono a " + forma + "!");
            rellenarEvolucionTab(b, datos, data);
        } else {
            b.set("#MsgEvolucion.Text", "Requisitos no cumplidos para " + forma);
        }
    }

    private void actualizarEnergias(UICommandBuilder b, DatosJugador datos) {
        if (datos == null) return;
        String labelA = "Energia A: " + (datos.companeroA != null ? datos.companeroA.energia + "/100" : "---");
        String labelB = "Energia B: " + (datos.companeroB != null ? datos.companeroB.energia + "/100" : "---");
        b.set("#LabelEnergias.Text", labelA + " | " + labelB);
    }

    private void actualizarEstadosCuidar(UICommandBuilder b, DatosJugador datos) {
        b.set("#EstadoA.Text", datos.companeroA != null
                ? "A - Hambre: " + datos.companeroA.hambre + " | Energia: " + datos.companeroA.energia
                : "A - Sin companero");
        b.set("#EstadoB.Text", datos.companeroB != null
                ? "B - Hambre: " + datos.companeroB.hambre + " | Energia: " + datos.companeroB.energia
                : "B - Sin companero");
    }

    private String aplicarCuidado(String accion, DatosJugador datos) {
        return switch (accion) {
            case "alimentar_a"  -> cuidar(datos.companeroA, "alimentar");
            case "alimentar_b"  -> cuidar(datos.companeroB, "alimentar");
            case "mimar_a"      -> cuidar(datos.companeroA, "mimar");
            case "mimar_b"      -> cuidar(datos.companeroB, "mimar");
            case "descansar_ab" -> cuidar(datos.companeroA, "descansar") + "  " +
                    cuidar(datos.companeroB, "descansar");
            default -> "";
        };
    }

    private String cuidar(DatoDigimon d, String accion) {
        if (d == null || !d.vivo) return "Sin companero";
        return switch (accion) {
            case "alimentar"  -> { d.alimentar(30);    yield d.nombre + " Hambre:" + d.hambre; }
            case "mimar"      -> { d.interactuar();    yield d.nombre + " Lazo:" + d.lazo; }
            case "descansar"  -> { d.descansar();      yield d.nombre + " HP:" + d.hp + " EN:" + d.energia; }
            default -> "";
        };
    }

    private String[][] evolucionesParaEspecie(String especie) {
        return switch (especie) {
            case "Botamon" -> new String[][]{{"Agumon","Agumon"},{"Gabumon","Gabumon"},{"Patamon","Patamon"}};
            case "Kuramon" -> new String[][]{{"Kapurimon","Kapurimon"},{"Terriermon","Terriermon"}};
            default        -> new String[][]{};
        };
    }

    private String capitalizar(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
