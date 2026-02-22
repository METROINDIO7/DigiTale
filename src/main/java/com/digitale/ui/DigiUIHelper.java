package com.digitale.ui;

import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.ui.builder.EventData;

// TODO: una vez conocida la ruta de CustomUIEventBindingType, añadir el import aquí
// import com.hypixel.hytale.???.CustomUIEventBindingType;

/**
 * Centraliza el binding de eventos de UI.
 * Cuando tengamos el paquete correcto de CustomUIEventBindingType,
 * solo modificamos este archivo y todo lo demás compila solo.
 */
public class DigiUIHelper {

    /**
     * Registra un click en un elemento UI que asigna un valor string a una variable codec.
     *
     * Según la documentación oficial:
     *   eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#id",
     *       EventData.of("@var", "#id.Value"), false);
     *
     * TODO: sustituir CustomUIEventBindingType.ValueChanged por la constante correcta
     *       y ajustar EventData según la firma real de addEventBinding.
     */
    public static void bindClick(UIEventBuilder eventBuilder,
                                 String elementId,
                                 String codecKey,
                                 String valor) {
        // PLACEHOLDER - reemplazar con la llamada real cuando tengamos el javap:
        // eventBuilder.addEventBinding(CustomUIEventBindingType.Click, elementId,
        //     EventData.of(codecKey, valor), false);

        // Por ahora usamos ValueChanged (que sí mencionan en la doc) como fallback
        // eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, elementId,
        //     EventData.of(codecKey, valor), false);
    }
}
