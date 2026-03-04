# Sistema de Almacenamiento Persistente de Datos del Jugador - DigiTale

## Resumen

Se ha implementado un **sistema de almacenamiento persistente de datos del jugador** usando la arquitectura **ECS (Entity Component System)** de Hytale. Este sistema permite guardar automáticamente el progreso (niveles y experiencia) de los compañeros del jugador en el perfil del servidor.

---

## Componentes Implementados

### 1. **PetProgressComponent**
Componente persistente que almacena el progreso de ambos compañeros:
- **LevelA**: Nivel del compañero A
- **ExperienceA**: Experiencia del compañero A (0-100)
- **LevelB**: Nivel del compañero B
- **ExperienceB**: Experiencia del compañero B (0-100)

**Ubicación**: `src/main/java/com/digitale/componentes/PetProgressComponent.java`

**Características**:
- Sistema de codificación BSON/JSON automático mediante `BuilderCodec`
- Sincronización automática al desconectarse/reconectarse
- Métodos auxiliares para añadir XP y resetear progreso

### 2. **ComponentRegistry**
Registro global que mantiene referencias a los tipos de componentes:

**Ubicación**: `src/main/java/com/digitale/componentes/ComponentRegistry.java`

**Métodos disponibles**:
```java
// Registrar el ComponentType (llamado automáticamente en setup())
ComponentRegistry.registerPetProgressType(type);

// Obtener el progreso de un jugador
PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);

// Obtener o crear el progreso de un jugador
PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
```

---

## Comandos Disponibles

### 1. `/digi_progress`
Muestra el nivel y experiencia actual de ambos compañeros.

**Uso**:
```
/digi_progress
```

**Salida**:
```
═══════ Progreso de tus Compañeros ═══════

[A] Compañero A
  Nivel: 5 | Experiencia: 45/100
  ████████░░░░░░░░░░░░

[B] Compañero B
  Nivel: 3 | Experiencia: 78/100
  ███████████████░░░░░

Usa /digi_entrenar para ganar experiencia.
═════════════════════════════════════════
```

### 2. `/digi_resetprogress [a|b|all]`
Comando de administrador para resetear el progreso.

**Uso**:
```
/digi_resetprogress all     # Resetea ambos compañeros
/digi_resetprogress a       # Resetea solo el compañero A
/digi_resetprogress b       # Resetea solo el compañero B
```

---

## Cómo Integrar con el Sistema Existente

### Opción 1: Usar en Comandos Existentes

En cualquier comando que extienda de `AbstractPlayerCommand`, puedes acceder al progreso así:

```java
@Override
protected void execute(@NonNullDecl CommandContext ctx,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl Ref<EntityStore> ref,
                       @NonNullDecl PlayerRef playerRef,
                       @NonNullDecl World world) {

    // Ejecutar en el hilo del mundo (IMPORTANTE)
    world.execute(() -> {
        // Obtener el progreso
        PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);
        
        if (progress != null) {
            int levelA = progress.getLevelA();
            int expA = progress.getExperienceA();
            
            // Hacer algo con los datos...
        }
    });
}
```

### Opción 2: Añadir XP después de Combate

En el sistema de batalla, puedes añadir XP de este modo:

```java
world.execute(() -> {
    PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
    
    // Añadir 50 XP al compañero A
    boolean levelUp = progress.addExperienceA(50);
    if (levelUp) {
        ctx.sendMessage(Message.raw("§6¡" + datos.companeroA.nombre + " alcanzó nivel " + progress.getLevelA() + "!"));
    }
});
```

### Opción 3: Obtener Datos en Eventos

En manejadores de eventos, usa el mismo patrón:

```java
getEventRegistry().registerGlobal(
    PlayerReadyEvent.class,
    event -> {
        Ref<EntityStore> playerStoreRef = event.getPlayerRef();
        Store<EntityStore> store = playerStoreRef.getStore();
        store.getExternalData().getWorld().execute(() -> {
            PetProgressComponent progress = ComponentRegistry.getProgress(store, playerStoreRef);
            if (progress != null) {
                getLogger().at(Level.INFO).log("Jugador tiene: Lvl A=" + progress.getLevelA());
            }
        });
    }
);
```

---

## Ejemplo: Integración con DigiEntrenarComando

Para integrar el sistema de progreso con el comando de entrenamiento existente:

```java
public class DigiEntrenarComando extends AbstractPlayerCommand {
    
    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, 
                          Ref<EntityStore> ref, PlayerRef playerRef, World world) {
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        
        // ... código de validación ...
        
        // Al finalizar el entrenamiento
        world.execute(() -> {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            
            // Calcular XP ganado (ejemplo: 30 XP)
            int xpGanado = 30;
            
            if (datos.companeroA != null && datos.companeroA.vivo) {
                boolean levelUpA = progress.addExperienceA(xpGanado);
                if (levelUpA) {
                    ctx.sendMessage(Message.raw("§6¡" + datos.companeroA.nombre + 
                        " ¡evolucionó a nivel " + progress.getLevelA() + "!"));
                }
            }
            
            if (datos.companeroB != null && datos.companeroB.vivo) {
                boolean levelUpB = progress.addExperienceB(xpGanado);
                if (levelUpB) {
                    ctx.sendMessage(Message.raw("§6¡" + datos.companeroB.nombre + 
                        " ¡evolucionó a nivel " + progress.getLevelB() + "!"));
                }
            }
        });
    }
}
```

---

## Arquitectura Interna

### Flujo de Datos

```
Jugador se conecta
    ↓
PlayerReadyEvent dispara
    ↓
Store carga componentes del jugador
    ↓
PetProgressComponent se deserializa automáticamente
    ↓
ComponentRegistry lo registra
    ↓
Comandos pueden acceder mediante ComponentRegistry.getProgress()
    ↓
Cambios se guardan automáticamente en el servidor
    ↓
Jugador se desconecta
    ↓
Store serializa y guarda PetProgressComponent en disco
```

### Codificación BSON/JSON

El componente se serializa automáticamente usando `BuilderCodec`:

```
{
  "LevelA": 5,
  "ExperienceA": 45,
  "LevelB": 3,
  "ExperienceB": 78
}
```

---

## Consideraciones de Seguridad

### ✅ Thread Safety
Todos los accesos al componente **DEBEN** ocurrir dentro de `world.execute()`:

```java
// ❌ INCORRECTO - puede causar desincronización
PetProgressComponent progress = store.getComponent(ref, type);

// ✅ CORRECTO - seguro para hilos
world.execute(() -> {
    PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);
});
```

### ✅ Permisos
Los comandos de administrador pueden restringirse añadiendo:

```java
public DigiResetProgressComando(...) {
    super(name, description);
    requirePermission(HytalePermissions.fromCommand("admin"));
}
```

---

## Próximos Pasos Recomendados

1. **Integrar con DigiEntrenarComando**: Usar el progreso para modificar estadísticas
2. **Integrar con DigiBatallaComando**: Ganar XP al ganar combates
3. **Añadir evoluciones**: Evolucionar automáticamente al alcanzar cierto nivel
4. **Persistencia de estadísticas**: Guardar ATK, DEF, SPD, etc. (similar a progreso)
5. **UI de progreso**: Mostrar barra de XP en HUDs

---

## Archivos Creados/Modificados

### ✅ Creados:
- `src/main/java/com/digitale/componentes/PetProgressComponent.java`
- `src/main/java/com/digitale/componentes/ComponentRegistry.java`
- `src/main/java/com/digitale/comandos/DigiProgressComando.java`
- `src/main/java/com/digitale/comandos/admin/DigiResetProgressComando.java`

### ✅ Modificados:
- `src/main/java/com/digitale/DigiTale.java` - Registro del componente

---

## Compilación

El proyecto compila exitosamente con Gradle:

```bash
./gradlew build
```

Build status: ✅ **SUCCESSFUL**
