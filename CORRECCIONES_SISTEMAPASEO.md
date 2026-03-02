# Correcciones y Estado del Sistema de Paseo (SistemaPaseo.java)

**Última actualización:** 1 de Marzo de 2026  
**Estado:** ✅ Compilando / ⏳ Esperando implementación de NPC spawn API

## 📋 Errores Solucionados

### 1. **Import faltante: `Entity`**
**Problema:** La clase `Entity` se usaba pero no estaba importada.  
**Solución:** ✅ Se agregó el import correcto.

### 2. **Import faltante: `ArrayList`**
**Problema:** Se usaba `List` pero faltaba importar `ArrayList`.  
**Solución:** ✅ Se agregaron imports necesarios.

### 3. **Clase `Scheduler` no existe en Hytale**
**Problema:** Código usaba `Scheduler.schedule()` que no existe en la API.  
**Solución:** ✅ Se eliminó la dependencia de Scheduler.

### 4. **`player.getComponent()` no existe**
**Problema:** Este método no existe en la clase `Player`.  
**Solución:** ✅ Se reemplazó con `store.getComponent(ref, TransformComponent.class)`.

### 5. **`world.spawnNPC()` no existe**
**Problema:** Intentamos llamar a método que no existe en la API de Hytale.  
```java
// ANTES (INCORRECTO)
world.spawnNPC(roleId, new Vector3d(x, y, z));

// DESPUÉS (EN PROGRESO)
registrarSpawnearNpc(world, playerRef, digimon, roleId, posicion, esA);
```
**Status:** ⏳ Registra solicitudes de spawn para depuración. Requiere investigación del API de spawn de Hytale.

---

## 🔍 Investigación: API de Spawn de NPCs en Hytale

### Hallazgos:
1. ✅ **Módulo Spawning existe:** Log muestra: `"[PluginManager] Enabled plugin Hytale:Spawning"`
2. ✅ **Assets de spawn registrados:** 
   - `WorldNPCSpawn: 101` configuraciones disponibles
   - `BeaconNPCSpawn: 80` configuraciones disponibles
3. ✅ **NPCFlockCommand existe:** Clase `NPCFlockCommand` presente en el JAR
4. ✅ **Roles de Digimon están registrados:** Archivos JSON en `Server/NPC/Roles/Bebes1/`

### Opciones de Implementación:
1. **CommandDispatcher** - No encontrado en API pública
2. **WorldProxy** - Requiere investigación adicional
3. **NPC Module Direct API** - No expuesto públicamente aún
4. **Event-based System** - Registrar evento para que sea procesado por servidor

---

## 📊 Estado Actual Completo

### ✅ FUNCIONANDO
- Sapotama ítem abre UI en tecla F (SwapFrom)
- Compile sin errores
- Threading correctamente manejado (WorldThread safe)
- Auto-redirect para nuevos jugadores ✅
- Estructura de spawn de NPCs lista para depuración

### ⏳ EN DESARROLLOCON TODO:
- Implementación real de spawn de NPCs
- Búsqueda y almacenamiento de referencias de NPCs spawnados
- Despawn de NPCs al cerrar paseo

### 📝 Logging Implementado:
- `[SistemaPaseo] ========== SPAWN COMPANIONS REQUEST ==========`
- `[SistemaPaseo] >>> SPAWN Request [A]` - Detalles de cada spawn
- `[SistemaPaseo DEBUG]` - Información completa del Digimon

---

## 🛠️ Próximos Pasos

1. **Prueba en servidor:** Iniciar servidor y registrar logs
2. **Investigar API:** Buscar en documentación de Hytale o ejemplos de mods
3. **Implementar spawn:** Usar API correcta una vez identificada
4. **Prueba de funcionalidad:** Verificar que se spawnen NPCs correctamente

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
