package com.digitale;

import com.digitale.comandos.PruebaComando;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class DigiTale extends JavaPlugin {

    public DigiTale(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup(){
        super.setup();

        //Todo lo que se haga acà, ya es lo que quieras ahcer para el plugin

        this.getCommandRegistry().registerCommand(
                new PruebaComando("saludar", "Saluda al usuairo"));


    }

}