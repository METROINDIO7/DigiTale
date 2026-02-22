package com.digitale;

import com.digitale.comandos.*;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class DigiTale extends JavaPlugin {

    public DigiTale(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        // ── Menú principal (UI) ────────────────────────────────────
        this.getCommandRegistry().registerCommand(
            new DigiMenuComando("digi_menu",
                "Abre el menu principal de DigiTale con UI"));

        // ── Comandos de texto (alternativa sin UI) ────────────────
        this.getCommandRegistry().registerCommand(
            new DigiStartComando("digi_start",
                "Elige tus companeros: /digi_start <esp1> <esp2> <nom1> [nom2]"));

        this.getCommandRegistry().registerCommand(
            new DigiStatusComando("digi_status",
                "Ver estado del equipo"));

        this.getCommandRegistry().registerCommand(
            new DigiEntrenarComando("digi_entrenar",
                "Entrenar: /digi_entrenar <a|b|ambos> <atk|def|spd|wis|hp> [sesiones]"));

        this.getCommandRegistry().registerCommand(
            new DigiBatallaComando("digi_batalla",
                "Combate: /digi_batalla [atacar|defender|huir|tactica <a|b> <tipo>]"));

        this.getCommandRegistry().registerCommand(
            new DigiCuidarComando("digi_cuidar",
                "Cuidar: /digi_cuidar <a|b|ambos> [alimentar|mimar|descansar]"));

        this.getCommandRegistry().registerCommand(
            new DigiEvolucionarComando("digi_evolucionar",
                "Evolucionar: /digi_evolucionar [a|b] [forma]"));
    }
}
