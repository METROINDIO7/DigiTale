package com.digitale.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

public class DigiUIHelper {

    public static void bindClick(UIEventBuilder eventBuilder,
                                 String elementId,
                                 String codecKey,
                                 String valor) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                elementId,
                new EventData().append(codecKey, valor),
                false
        );
    }
}
