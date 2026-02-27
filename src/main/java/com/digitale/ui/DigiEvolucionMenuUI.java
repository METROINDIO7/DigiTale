package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.ui.DigiUIHelper;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DigiEvolucionMenuUI extends InteractiveCustomUIPage<DigiEvolucionMenuUI.Data> {

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec
            .builder(Data.class, Data::new)
            .append(new KeyedCodec<>("Accion", Codec.STRING),
                    (data, v) -> data.accion = v,
                    data -> data.accion)
            .add()
            .build();
        public String accion = "";
    }

    private final PlayerRef playerRef;
    private String slotActivo; // "a" o "b"

    // Evoluciones mostradas actualmente en los 3 botones
    private final String[] evosActuales = {"", "", ""};

    public DigiEvolucionMenuUI(@Nonnull PlayerRef playerRef, @Nonnull String slotInicial) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
        this.slotActivo = slotInicial;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("Pages/DigiEvolucionMenu.ui");

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        rellenarDiGimon(uiBuilder, datos);

        // Tabs
        DigiUIHelper.bindClick(eventBuilder, "#TabA", "Accion", "tab_a");
        DigiUIHelper.bindClick(eventBuilder, "#TabB", "Accion", "tab_b");

        // Botones de evolucion (cada uno manda su indice)
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvo1", "Accion", "evo_0");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvo2", "Accion", "evo_1");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvo3", "Accion", "evo_2");

        DigiUIHelper.bindClick(eventBuilder, "#BtnVolver", "Accion", "volver");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());

        switch (data.accion) {
            case "volver" -> {
                if (player != null)
                    player.getPageManager().openCustomPage(ref, store, new DigiMainMenuUI(playerRef));
            }
            case "tab_a" -> {
                slotActivo = "a";
                UICommandBuilder b = new UICommandBuilder();
                rellenarDiGimon(b, datos);
                sendUpdate(b, null, false);
            }
            case "tab_b" -> {
                slotActivo = "b";
                UICommandBuilder b = new UICommandBuilder();
                rellenarDiGimon(b, datos);
                sendUpdate(b, null, false);
            }
            default -> {
                if (data.accion.startsWith("evo_")) {
                    int idx = Integer.parseInt(data.accion.substring(4));
                    intentarEvolucionar(idx, datos, ref, store);
                } else {
                    sendUpdate(new UICommandBuilder(), null, false);
                }
            }
        }
    }

    private void intentarEvolucionar(int idx, DatosJugador datos,
                                     Ref<EntityStore> ref, Store<EntityStore> store) {
        if (idx < 0 || idx >= evosActuales.length || evosActuales[idx].isEmpty()) {
            sendUpdate(new UICommandBuilder(), null, false); return;
        }
        DatoDigimon d = slotActivo.equals("a") ? datos.companeroA : datos.companeroB;
        if (d == null || !d.vivo) { sendUpdate(new UICommandBuilder(), null, false); return; }

        String forma = evosActuales[idx];
        UICommandBuilder b = new UICommandBuilder();

        if (d.puedeEvolucionar(forma)) {
            aplicarEvolucion(d, forma);
            b.set("#MsgEvolucion.Text", "" + d.nombre + " evoluciono a " + forma + "!");
            rellenarDiGimon(b, datos);
        } else {
            b.set("#MsgEvolucion.Text", "Aun no cumples los requisitos para " + forma);
        }
        sendUpdate(b, null, false);
    }

    // Rellenar datos del Digimon activo
    private void rellenarDiGimon(UICommandBuilder b, DatosJugador datos) {
        DatoDigimon d = slotActivo.equals("a") ? datos.companeroA : datos.companeroB;
        if (d == null || !d.vivo) {
            b.set("#InfoNombre.Text", "Sin companero");
            b.set("#InfoEspecie.Text", "-");
            b.set("#InfoStats.Text", "-");
            limpiarEvo(b);
            return;
        }
        b.set("#InfoNombre.Text", d.nombre);
        b.set("#InfoEspecie.Text", d.especie + " - " + d.nombreNivel() + " - " + d.elemento);
        b.set("#InfoStats.Text", 
            "ATK:" + d.atk + " DEF:" + d.def + " SPD:" + d.spd +
            " WIS:" + d.wis + " | V:" + d.victorias + " Lazo:" + d.lazo + " ABI:" + d.abi);

        // Cargar evoluciones posibles
        String[][] opciones = evolucionesParaEspecie(d.especie);
        String[] btnIds = {"#BtnEvo1", "#BtnEvo2", "#BtnEvo3"};

        for (int i = 0; i < 3; i++) {
            if (i < opciones.length) {
                evosActuales[i] = opciones[i][0];
                boolean puede = d.puedeEvolucionar(opciones[i][0]);
                String icono = puede ? "[OK] " : "[X] ";
                b.set(btnIds[i]  + ".Text", icono + opciones[i][0] + " - " + opciones[i][1]);
            } else {
                evosActuales[i] = "";
                b.set(btnIds[i] + ".Text", "-");
            }
        }
    }

    private void limpiarEvo(UICommandBuilder b) {
        for (int i = 0; i < 3; i++) evosActuales[i] = "";
        b.set("#BtnEvo1.Text", "---");
        b.set("#BtnEvo2.Text", "---");
        b.set("#BtnEvo3.Text", "---");
    }

    // Arbol de evoluciones
    private String[][] evolucionesParaEspecie(String especie) {
        return switch (especie) {
            case "Botamon"  -> new String[][]{{"Koromon",       "Lazo>=30 o 1 victoria"}};
            case "Punimon"  -> new String[][]{{"Tsunomon",      "Lazo>=30 o 1 victoria"}};
            case "Poyomon"  -> new String[][]{{"Tokomon",       "Lazo>=30 o 1 victoria"}};
            case "Yuramon"  -> new String[][]{{"Tanemon",       "Lazo>=30 o 1 victoria"}};
            case "Pichimon" -> new String[][]{{"Bukamon",       "Lazo>=30 o 1 victoria"}};
            case "Nyokimon" -> new String[][]{{"Yokomon",       "Lazo>=30 o 1 victoria"}};
            case "Koromon"  -> new String[][]{{"Agumon",        "3 victorias, lazo>=40"}};
            case "Tsunomon" -> new String[][]{{"Gabumon",       "3 victorias, def>=12"}};
            case "Tokomon"  -> new String[][]{{"Patamon",       "3 victorias, wis>=12"}};
            case "Tanemon"  -> new String[][]{{"Palmon",        "3 victorias, wis>=11"}};
            case "Bukamon"  -> new String[][]{{"Gomamon",       "3 victorias, spd>=14"}};
            case "Yokomon"  -> new String[][]{{"Biyomon",       "3 victorias, spd>=15"}};
            case "Agumon"   -> new String[][]{{"Greymon",       "10V, lazo>=50"}};
            case "Gabumon"  -> new String[][]{{"GarurumonA",    "10V, def>=20"}};
            case "Patamon"  -> new String[][]{{"Angemon",       "10V, wis>=20"}};
            case "Palmon"   -> new String[][]{{"Togemon",       "10V, def>=18"}};
            case "Gomamon"  -> new String[][]{{"Ikkakumon",     "10V, maxHp>=160"}};
            case "Biyomon"  -> new String[][]{{"Birdramon",     "10V, spd>=22"}};
            case "Greymon"      -> new String[][]{{"MetalGreymon",   "25V, atk>=40, lazo>=70"}};
            case "GarurumonA"   -> new String[][]{{"WereGarurumon",  "25V, def>=45, disc>=60"}};
            case "Angemon"      -> new String[][]{{"MagnaAngemon",   "25V, wis>=45"}};
            case "Togemon"      -> new String[][]{{"Lillymon",       "25V, wis>=40"}};
            case "Ikkakumon"    -> new String[][]{{"Zudomon",        "25V, def>=50"}};
            case "Birdramon"    -> new String[][]{{"Garudamon",      "25V, spd>=50"}};
            case "MetalGreymon" -> new String[][]{{"WarGreymon",     "50V, atk>=80, lazo>=90, abi>=20"}};
            case "WereGarurumon"-> new String[][]{{"MetalGarurumon", "50V, def>=80, abi>=20"}};
            case "MagnaAngemon" -> new String[][]{{"Seraphimon",     "50V, wis>=90, abi>=20"}};
            case "Lillymon"     -> new String[][]{{"Rosemon",        "50V, wis>=85, lazo>=85, abi>=20"}};
            case "Zudomon"      -> new String[][]{{"MarineAngemon",  "50V, maxHp>=400, abi>=20"}};
            case "Garudamon"    -> new String[][]{{"Phoenixmon",     "50V, spd>=90, abi>=20"}};
            default -> new String[0][0];
        };
    }

    private void aplicarEvolucion(DatoDigimon d, String forma) {
        String anterior = d.especie;
        d.especie = forma;
        d.abi += 5;
        switch (forma) {
            case "Koromon"  -> { d.nivel=2; d.maxHp+=25; d.atk+=4; d.def+=3; d.spd+=2; d.wis+=2; }
            case "Tsunomon" -> { d.nivel=2; d.maxHp+=22; d.atk+=3; d.def+=4; d.spd+=2; d.wis+=3; }
            case "Tokomon"  -> { d.nivel=2; d.maxHp+=20; d.atk+=3; d.def+=2; d.spd+=3; d.wis+=5; }
            case "Tanemon"  -> { d.nivel=2; d.maxHp+=24; d.atk+=3; d.def+=3; d.spd+=2; d.wis+=4; }
            case "Bukamon"  -> { d.nivel=2; d.maxHp+=24; d.atk+=3; d.def+=3; d.spd+=4; d.wis+=2; }
            case "Yokomon"  -> { d.nivel=2; d.maxHp+=22; d.atk+=3; d.def+=2; d.spd+=5; d.wis+=3; }
            case "Agumon"   -> { d.nivel=3; d.maxHp+=55; d.atk+=7; d.def+=4; d.spd+=4; d.wis+=3; d.elemento="FUEGO"; }
            case "Gabumon"  -> { d.nivel=3; d.maxHp+=50; d.atk+=6; d.def+=9; d.spd+=5; d.wis+=4; d.elemento="HIELO"; }
            case "Patamon"  -> { d.nivel=3; d.maxHp+=45; d.atk+=6; d.def+=4; d.spd+=6; d.wis+=7; d.elemento="LUZ"; }
            case "Palmon"   -> { d.nivel=3; d.maxHp+=50; d.atk+=5; d.def+=6; d.spd+=4; d.wis+=6; d.elemento="NATURALEZA"; }
            case "Gomamon"  -> { d.nivel=3; d.maxHp+=53; d.atk+=5; d.def+=4; d.spd+=7; d.wis+=4; d.elemento="AGUA"; }
            case "Biyomon"  -> { d.nivel=3; d.maxHp+=48; d.atk+=5; d.def+=3; d.spd+=9; d.wis+=5; d.elemento="VIENTO"; }
            case "Greymon"       -> { d.nivel=4; d.maxHp+=80;  d.atk+=15; d.def+=12; d.spd+=5;  d.wis+=5; }
            case "GarurumonA"    -> { d.nivel=4; d.maxHp+=70;  d.atk+=12; d.def+=18; d.spd+=8;  d.wis+=7; }
            case "Angemon"       -> { d.nivel=4; d.maxHp+=60;  d.atk+=13; d.def+=10; d.spd+=10; d.wis+=15; }
            case "Togemon"       -> { d.nivel=4; d.maxHp+=75;  d.atk+=10; d.def+=16; d.spd+=5;  d.wis+=10; }
            case "Ikkakumon"     -> { d.nivel=4; d.maxHp+=90;  d.atk+=12; d.def+=14; d.spd+=4;  d.wis+=6; }
            case "Birdramon"     -> { d.nivel=4; d.maxHp+=65;  d.atk+=13; d.def+=9;  d.spd+=15; d.wis+=8; }
            case "MetalGreymon"  -> { d.nivel=5; d.maxHp+=120; d.atk+=25; d.def+=20; d.spd+=8;  d.wis+=10; }
            case "WereGarurumon" -> { d.nivel=5; d.maxHp+=100; d.atk+=20; d.def+=28; d.spd+=15; d.wis+=12; }
            case "MagnaAngemon"  -> { d.nivel=5; d.maxHp+=90;  d.atk+=20; d.def+=18; d.spd+=18; d.wis+=30; }
            case "Lillymon"      -> { d.nivel=5; d.maxHp+=85;  d.atk+=18; d.def+=16; d.spd+=20; d.wis+=28; }
            case "Zudomon"       -> { d.nivel=5; d.maxHp+=140; d.atk+=18; d.def+=32; d.spd+=6;  d.wis+=12; }
            case "Garudamon"     -> { d.nivel=5; d.maxHp+=95;  d.atk+=20; d.def+=15; d.spd+=28; d.wis+=14; }
            case "WarGreymon"    -> { d.nivel=6; d.maxHp+=200; d.atk+=40; d.def+=35; d.spd+=20; d.wis+=20; }
            case "MetalGarurumon"-> { d.nivel=6; d.maxHp+=180; d.atk+=35; d.def+=42; d.spd+=25; d.wis+=22; }
            case "Seraphimon"    -> { d.nivel=6; d.maxHp+=160; d.atk+=35; d.def+=30; d.spd+=30; d.wis+=50; }
            case "Rosemon"       -> { d.nivel=6; d.maxHp+=150; d.atk+=30; d.def+=28; d.spd+=35; d.wis+=45; }
            case "MarineAngemon" -> { d.nivel=6; d.maxHp+=220; d.atk+=28; d.def+=40; d.spd+=28; d.wis+=30; }
            case "Phoenixmon"    -> { d.nivel=6; d.maxHp+=170; d.atk+=38; d.def+=30; d.spd+=40; d.wis+=30; }
        }
        d.hp = d.maxHp;
    }
}
