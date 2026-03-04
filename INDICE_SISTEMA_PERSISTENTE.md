# Índice del Sistema de Almacenamiento Persistente

## Descripción General

Se ha implementado un **sistema de almacenamiento persistente de datos del jugador** para el mod DigiTale. Este sistema permite guardar automáticamente el progreso (niveles y experiencia) de los compañeros Digimon de cada jugador en el servidor.

---

## 📁 Archivos Creados/Modificados

### Componentes del Sistema

| Archivo | Ubicación | Descripción |
|---------|-----------|-------------|
| **PetProgressComponent.java** | `src/main/java/com/digitale/componentes/` | Componente persistente que almacena nivel y XP |
| **ComponentRegistry.java** | `src/main/java/com/digitale/componentes/` | Registro global del ComponentType |

### Comandos de Jugador

| Comando | Archivo | Descripción |
|---------|---------|-------------|
| `/digi_progress` | `src/main/java/com/digitale/comandos/DigiProgressComando.java` | Ver progreso actual |

### Comandos de Administrador

| Comando | Archivo | Descripción |
|---------|---------|-------------|
| `/digi_resetprogress` | `src/main/java/com/digitale/comandos/admin/DigiResetProgressComando.java` | Resetear progreso |
| `/digi_setprogress` | `src/main/java/com/digitale/comandos/admin/DigiSetProgressComando.java` | Setear progreso específico |

### Documentación

| Archivo | Descripción |
|---------|-------------|
| **SISTEMA_ALMACENAMIENTO_PERSISTENTE.md** | Guía técnica completa del sistema |
| **RESUMEN_IMPLEMENTACION.md** | Resumen de lo implementado y cómo usarlo |
| **INDICE_SISTEMA_PERSISTENTE.md** | Este archivo |

### Archivo Principal Modificado

| Archivo | Cambios |
|---------|---------|
| **DigiTale.java** | Registro del componente + comandos |

---

## 🔑 Puntos Clave

### 1. Persistencia Automática
- Los datos se guardan automáticamente en el perfil del servidor
- Se sincronizan al conectar/desconectar jugadores
- No requiere código adicional de guardado

### 2. Thread Safety
- Todos los accesos ocurren dentro de `world.execute()`
- No hay riesgos de desincronización

### 3. API Simple
```java
// Obtener progreso
PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);

// Obtener o crear
PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);

// Añadir XP
boolean levelUp = progress.addExperienceA(50);

// Resetear
progress.resetAll();
```

---

## 📊 Métodos Disponibles

### PetProgressComponent

```java
// Getters
int getLevelA/B()
int getExperienceA/B()

// Setters
void setLevelA/B(int level)
void setExperienceA/B(int exp)

// Operaciones
boolean addExperienceA/B(int exp)  // Retorna true si sube de nivel
void resetAll()                     // Resetea todo
void resetA/B()                     // Resetea un compañero
String getProgressString()          // Representación en texto
```

---

## 🎮 Ejemplos de Uso

### Mostrar Progreso
```java
PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);
System.out.println("Nivel A: " + progress.getLevelA());
System.out.println("XP A: " + progress.getExperienceA() + "/100");
```

### Ganar XP
```java
world.execute(() -> {
    PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
    boolean levelUp = progress.addExperienceA(50);
    if (levelUp) {
        System.out.println("¡Subió de nivel!");
    }
});
```

### En Eventos
```java
getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
    Ref<EntityStore> playerStoreRef = event.getPlayerRef();
    Store<EntityStore> store = playerStoreRef.getStore();
    store.getExternalData().getWorld().execute(() -> {
        PetProgressComponent progress = ComponentRegistry.getProgress(store, playerStoreRef);
        if (progress != null) {
            // Usar progreso...
        }
    });
});
```

---

## ✅ Estado de Compilación

```
BUILD SUCCESSFUL in 2s
```

---

## 📝 Próximos Pasos Recomendados

1. **Integrar con DigiEntrenarComando** - Ganar XP al entrenar
2. **Integrar con DigiBatallaComando** - Ganar XP al ganar combates  
3. **Sistema de evolución** - Evolucionar al alcanzar cierto nivel
4. **Estadísticas persistentes** - Guardar ATK, DEF, SPD, etc.
5. **UI en HUD** - Mostrar barras de progreso visualmente

---

## 🔗 Referencias

- **Guía técnica**: [SISTEMA_ALMACENAMIENTO_PERSISTENTE.md](SISTEMA_ALMACENAMIENTO_PERSISTENTE.md)
- **Resumen implementación**: [RESUMEN_IMPLEMENTACION.md](RESUMEN_IMPLEMENTACION.md)
- **Componente**: `com.digitale.componentes.PetProgressComponent`
- **Registro**: `com.digitale.componentes.ComponentRegistry`

---

## 🚀 Inicio Rápido

Para empezar a usar:

```java
// En cualquier comando/evento que extienda AbstractPlayerCommand
world.execute(() -> {
    PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
    
    // Leer datos
    int level = progress.getLevelA();
    int exp = progress.getExperienceA();
    
    // Modificar datos
    progress.addExperienceA(50);
    
    // Los cambios se guardan automáticamente
});
```

---

**Implementación completada exitosamente ✅**
