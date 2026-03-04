# Sistema de Almacenamiento Persistente - Resumen de Implementación

## ✅ Lo que se ha implementado

He creado un **sistema completo de almacenamiento persistente de datos del jugador** para tu mod DigiTale basado en la arquitectura ECS de Hytale. Este sistema permite guardar automáticamente el progreso (niveles y experiencia) de los compañeros.

---

## 📦 Archivos Creados

### 1. **PetProgressComponent.java**
- Componente persistente que almacena nivel y XP de ambos compañeros
- Incluye métodos para añadir experiencia y resetear progreso
- Usa `BuilderCodec` para serializar automáticamente los datos

**Ubicación**: `/src/main/java/com/digitale/componentes/PetProgressComponent.java`

**Características principales**:
- `addExperienceA(exp)` / `addExperienceB(exp)` - Añade XP y sube de nivel automáticamente
- `resetAll()` / `resetA()` / `resetB()` - Resetea progreso
- `getProgressString()` - Obtiene representación de texto del progreso

### 2. **ComponentRegistry.java**
- Registro global que mantiene el ComponentType para acceder al componente desde cualquier comando
- Simplifica el acceso al componente en toda la aplicación

**Ubicación**: `/src/main/java/com/digitale/componentes/ComponentRegistry.java`

**Métodos disponibles**:
```java
ComponentRegistry.getProgress(store, ref)           // Obtiene el progreso
ComponentRegistry.ensureAndGetProgress(store, ref)  // Obtiene o crea
```

### 3. **DigiProgressComando.java**
- Comando público `/digi_progress` que muestra el progreso actual
- Muestra barra visual de experiencia

**Ubicación**: `/src/main/java/com/digitale/comandos/DigiProgressComando.java`

### 4. **DigiResetProgressComando.java**
- Comando administrativo `/digi_resetprogress [a|b|all]`
- Permite resetear el progreso de compañeros individuales o ambos

**Ubicación**: `/src/main/java/com/digitale/comandos/admin/DigiResetProgressComando.java`

### 5. **DigiSetProgressComando.java**
- Comando administrativo `/digi_setprogress <a|b|both> <level> [exp]`
- Permite establecer nivel y XP específicos (útil para testing)

**Ubicación**: `/src/main/java/com/digitale/comandos/admin/DigiSetProgressComando.java`

### 6. **SISTEMA_ALMACENAMIENTO_PERSISTENTE.md**
- Documentación completa sobre el sistema implementado
- Incluye ejemplos de integración y patrones de uso

**Ubicación**: `/SISTEMA_ALMACENAMIENTO_PERSISTENTE.md`

---

## 📝 Archivos Modificados

### DigiTale.java
- ✅ Añadido import de `ComponentRegistry` y `PetProgressComponent`
- ✅ Añadido `ComponentType petProgressType` como variable de instancia
- ✅ Registrado el componente en `setup()`
- ✅ Registrados los tres comandos nuevos

---

## 🎮 Comandos Disponibles

### Para Jugadores

#### `/digi_progress`
Muestra el nivel y experiencia de ambos compañeros con barra visual.

```
═══════ Progreso de tus Compañeros ═══════
[A] Compañero A
  Nivel: 5 | Experiencia: 45/100
  ████████░░░░░░░░░░░░
```

### Para Administradores

#### `/digi_resetprogress [a|b|all]`
Resetea el progreso (nivel 1, 0 XP).

```
/digi_resetprogress all    # Resetea ambos
/digi_resetprogress a      # Solo A
/digi_resetprogress b      # Solo B
```

#### `/digi_setprogress <a|b|both> <level> [exp]`
Establece nivel y XP específicos.

```
/digi_setprogress a 10 50        # Compañero A: Nivel 10, 50 XP
/digi_setprogress both 5 0       # Ambos: Nivel 5, 0 XP
```

---

## 🔧 Cómo Integrar con tu Código Existente

### Ejemplo 1: Ganar XP en Combate

```java
world.execute(() -> {
    PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
    
    boolean levelUpA = progress.addExperienceA(50);  // Añadir 50 XP
    if (levelUpA) {
        int newLevel = progress.getLevelA();
        ctx.sendMessage(Message.raw("¡Subió a nivel " + newLevel + "!"));
    }
});
```

### Ejemplo 2: Mostrar Progreso

```java
PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);
if (progress != null) {
    int levelA = progress.getLevelA();
    int expA = progress.getExperienceA();
    // Usar estos valores...
}
```

### Ejemplo 3: En el Comando de Entrenamiento

Puedes integrar esto en `DigiEntrenarComando.java` para que ganen XP al entrenar:

```java
@Override
protected void execute(CommandContext ctx, Store<EntityStore> store, 
                      Ref<EntityStore> ref, PlayerRef playerRef, World world) {
    DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
    
    // ... validaciones ...
    
    world.execute(() -> {
        PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
        int xpGanado = 30;
        
        if (datos.companeroA != null && datos.companeroA.vivo) {
            boolean levelUp = progress.addExperienceA(xpGanado);
            if (levelUp) {
                ctx.sendMessage(Message.raw("¡" + datos.companeroA.nombre + 
                    " alcanzó nivel " + progress.getLevelA() + "!"));
            }
        }
    });
}
```

---

## ⚙️ Detalles Técnicos

### Thread Safety
✅ Todos los accesos al componente ocurren dentro de `world.execute()` para garantizar thread safety.

### Persistencia Automática
✅ El servidor se encarga automáticamente de:
- Serializar el componente al desconectarse el jugador
- Deserializarlo al conectarse de nuevo
- Guardar cambios en disco

### Flujo de Datos
```
Conexión → PlayerReady → Store carga componentes → 
ComponentRegistry accesible → Cambios automáticos → 
Desconexión → Guarda en disco
```

---

## 📊 Estructura de Datos Guarda

Los datos se guardan en formato BSON/JSON:

```json
{
  "LevelA": 5,
  "ExperienceA": 45,
  "LevelB": 3,
  "ExperienceB": 78
}
```

---

## ✅ Compilación

El proyecto compila exitosamente:

```bash
$ ./gradlew build
BUILD SUCCESSFUL in 2s
```

---

## 🚀 Próximos Pasos Recomendados

1. **Integrar XP en combates**: Ganar XP al ganar batallas en `DigiBatallaComando`
2. **Integrar XP en entrenamiento**: Modificar `DigiEntrenarComando` para ganar XP
3. **Sistema de evolución**: Evolucionar automáticamente al alcanzar cierto nivel
4. **Persistencia de estadísticas**: Guardar base ATK, DEF, SPD (similar a este sistema)
5. **Efectos visuales**: Mostrar barras de XP en HUDs

---

## 📚 Documentación Completa

Para más detalles:
- Ver: [SISTEMA_ALMACENAMIENTO_PERSISTENTE.md](SISTEMA_ALMACENAMIENTO_PERSISTENTE.md)

---

## 🐛 Notas Importantes

### Seguridad de Hilos
Siempre accede al componente dentro de `world.execute()`:

```java
// ❌ INCORRECTO
PetProgressComponent progress = store.getComponent(ref, type);

// ✅ CORRECTO
world.execute(() -> {
    PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);
});
```

### Componente Nulo
Si `getProgress()` retorna `null`, significa que aún no se ha inicializado. Usa `ensureAndGetProgress()` para crearlo automáticamente.

---

## 📞 Soporte

Si encuentras problemas:
1. Verifica que el componente está registrado en `DigiTale.setup()`
2. Asegúrate de acceder al componente dentro de `world.execute()`
3. Revisa los logs del servidor para mensajes de error
4. Compila el proyecto: `./gradlew build`

---

**¡Sistema de almacenamiento persistente completamente implementado y funcional!** ✅
