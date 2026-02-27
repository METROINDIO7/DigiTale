# Correcciones realizadas en SistemaPaseo.java

## Errores encontrados y solucionados

### 1. **Import faltante: `Entity`**
**Problema:** La clase `Entity` se usaba en el código pero no estaba importada.
```java
import com.hypixel.hytale.server.core.entity.Entity;
```
**Solución:** Se agregó el import correcto.

---

### 2. **Import faltante: `ArrayList`**
**Problema:** Se usaba `List` pero no se importaba `ArrayList`.
```java
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
```

---

### 3. **Clase `Scheduler` no existe en Hytale**
**Problema:** El código usaba `Scheduler.schedule()` que no existe en la API de Hytale.
```java
// ANTES (INCORRECTO)
Scheduler.schedule(1, () -> { ... });

// DESPUÉS (CORREGIDO)
// Búsqueda inmediata sin scheduler
List<Entity> entidades = getEntitiesInArea(world, pos, 3);
```
**Solución:** Se eliminó la dependencia de `Scheduler`.

---

### 4. **`player.getComponent()` no existe**
**Problema:** El método `getComponent()` no existe en la clase `Player`.
```java
// ANTES (INCORRECTO)
TransformComponent transform = player.getComponent(TransformComponent.class);

// DESPUÉS (CORRECTO)
TransformComponent transform = store.getComponent(ref, TransformComponent.class);
```
**Solución:** Se obtiene el componente desde `store` en lugar de desde `player`.

---

### 5. **`player.executeCommand()` no existe**
**Problema:** El método `executeCommand()` no existe en la clase `Player`.
```java
// ANTES (INCORRECTO)
player.executeCommand("npc spawn " + rolA + " " + posA.x + " " + posA.y + " " + posA.z);

// DESPUÉS (COMENTADO - PENDIENTE DE IMPLEMENTACIÓN)
// TODO: Reemplazar con el método correcto de Hytale para ejecutar comandos
// Opciones: player.runCommand(), world.executeCommand(), o usar API de NPCs
```
**Solución:** Se comentó el código y se agregó un TODO. Necesitas investigar el método correcto en la API de Hytale.

---

### 6. **`getUuid()` está deprecado**
**Problema:** El método `getUuid()` está marcado como deprecado y será removido. Sin embargo, `getId()` no existe en `PlayerRef`, y `getUuid()` es el único método disponible en toda la API de Hytale.

**Solución aplicada:**
```java
// 1. Anotación a nivel de clase
@SuppressWarnings("deprecation")
public class SistemaPaseo {
    
    // 2. Método helper que encapsula el uso deprecado
    private static UUID obtenerUuidJugador(PlayerRef playerRef) {
        return playerRef.getUuid();
    }
    
    // 3. Uso del método helper
    UUID playerUuid = obtenerUuidJugador(playerRef);
}
```

**Justificación:**
- `getUuid()` es el **único método disponible** en la API de Hytale para obtener el UUID
- Todo el proyecto lo usa (verificado en todos los archivos)
- La anotación `@SuppressWarnings("deprecation")` a nivel de clase suprime la advertencia
- El método helper encapsula el uso deprecado de forma limpia y documentada
- Esta es la forma estándar en Java para manejar métodos deprecados sin alternativas

---

### 7. **`world.removeEntity()` no existe**
**Problema:** El método `removeEntity()` no existe en la clase `World`.
```java
// ANTES (INCORRECTO)
world.removeEntity(datos.refPaseoA);

// DESPUÉS (COMENTADO - PENDIENTE DE IMPLEMENTACIÓN)
// TODO: Reemplazar con el método correcto de Hytale para eliminar entidades
// Opciones: ref.delete(), world.removeEntity(), o usar API de NPCs
```
**Solución:** Se comentó el código y se agregó un TODO. Necesitas investigar el método correcto en la API de Hytale.

---

### 8. **Método `getEntitiesInArea()` sin implementación**
**Problema:** El método lanzaba `UnsupportedOperationException`.
```java
private static List<Entity> getEntitiesInArea(World world, Vector3d center, double radius) {
    throw new UnsupportedOperationException("Implementa getEntitiesInArea según la API de Hytale");
}
```
**Solución:** Se mantiene como placeholder con comentarios sobre cómo implementarlo.

---

## Resumen de cambios

| Error | Solución |
|-------|----------|
| ❌ `player.getComponent()` | ✅ Usar `store.getComponent(ref, ...)` |
| ❌ `player.executeCommand()` | ⚠️ TODO: Investigar método correcto |
| ❌ `getUuid()` deprecado | ✅ Usar `@SuppressWarnings("deprecation")` |
| ❌ `getId()` no existe | ✅ Mantener `getUuid()` |
| ❌ `world.removeEntity()` | ⚠️ TODO: Investigar método correcto |
| ❌ `Scheduler` no existe | ✅ Eliminado |
| ❌ Imports faltantes | ✅ Agregados |
| ❌ `TransformComponent` no es `Component<EntityStore>` | ✅ Usar posición por defecto (TODO) |

---

## Próximos pasos recomendados

1. **Implementar spawn de NPCs** - Investigar cómo ejecutar comandos o usar API de NPCs en Hytale
2. **Implementar eliminación de NPCs** - Investigar cómo eliminar entidades en Hytale
3. **Implementar `getEntitiesInArea()`** - Según la API real de Hytale
4. **Probar el código** - En el servidor de Hytale para validar que funciona correctamente

---

## Archivos modificados
- `/src/main/java/com/digitale/sistema/SistemaPaseo.java`
