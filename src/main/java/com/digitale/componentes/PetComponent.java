package com.digitale.componentes;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;

/**
 * Componente personalizado para rastrear mascotas spawnadas.
 * Se guarda en el jugador para mantener referencias a los NPCs invocados.
 */
public class PetComponent implements Component {
    
    // Referencia al NPC compañero A
    private Ref petRefA;
    
    // Referencia al NPC compañero B  
    private Ref petRefB;
    
    // Guarda si el paseo está activo
    private boolean paseoActivo;

    public static final BuilderCodec<PetComponent> CODEC = BuilderCodec.builder(PetComponent.class, PetComponent::new)
        .build();

    public PetComponent() {
        this.petRefA = null;
        this.petRefB = null;
        this.paseoActivo = false;
    }

    public PetComponent(PetComponent other) {
        this.petRefA = other.petRefA;
        this.petRefB = other.petRefB;
        this.paseoActivo = other.paseoActivo;
    }

    @Override
    public Component clone() {
        return new PetComponent(this);
    }

    // ─── Getters y Setters ───
    
    public Ref getPetRefA() {
        return petRefA;
    }

    public void setPetRefA(Ref ref) {
        this.petRefA = ref;
    }

    public Ref getPetRefB() {
        return petRefB;
    }

    public void setPetRefB(Ref ref) {
        this.petRefB = ref;
    }

    public boolean isPaseoActivo() {
        return paseoActivo;
    }

    public void setPaseoActivo(boolean activo) {
        this.paseoActivo = activo;
    }
    
    /**
     * Verifica si una mascota es válida y existe
     */
    public boolean isPetValid(Ref petRef) {
        return petRef != null && petRef.isValid();
    }
}
