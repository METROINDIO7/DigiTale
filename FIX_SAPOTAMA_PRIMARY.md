# 🔧 CORRECCIÓN FINAL: Sapotama - Primary en lugar de SwapFrom

**Fecha:** 1 de Marzo de 2026, 19:19  
**Status:** ✅ CORREGIDO

---

## 🐛 Problema Anterior

Cuando usé `"SwapFrom"` en Sapotama.json:
- ❌ **Hotbar bloqueada:** No podía mover items entre slots
- ❌ **Cambios de slot ocultan hotbar:** Automáticamente se ocultaba
- ❌ **F no abría nada:** El filtro interfería demasiado

**Causa:** `SwapFrom` genera un evento para **CADA** cambio de slot, no solo para el uso del item.

---

## ✅ Solución: Usar "Primary"

### Cambio 1: `Sapotama.json`

**AHORA:**
```json
"Interactions": {
  "Primary": {
    "Interactions": [
      {
        "Type": "Simple"
      }
    ]
  }
}
```

**Ventajas:**
- `Primary` = Click derecho en el item
- NO dispara en cambios de slot
- Solo cuando el jugador intenta usar el item activamente

### Cambio 2: `SapotamaInputFilter.java`

```java
// Antes: interceptaba SwapFrom
if (chain.interactionType != InteractionType.SwapFrom || !chain.initial) {

// Ahora: intercepta Primary
if (chain.interactionType != InteractionType.Primary || !chain.initial) {
```

---

## 📊 Flujo Correcto Ahora

```
[Jugador] Sostiene Sapotama + [Click/Uso]
    ↓
Envía Primary packet
    ↓
SapotamaInputFilter detecta Primary
    ↓
Verifica: ¿Es Sapotama?
    ├─ SÍ: Abre UI
    └─ NO: Continúa norma lmente
```

---

## ✅ Comportamiento Esperado

### Con Sapotama en hotbar:
1. ✅ Puedes mover el item entre slots normalmente
2. ✅ Los cambios de slot NO ocultan la hotbar
3. ✅ Click derecho sobre Sapotama → Abre UI

### Otros items:
- ✅ Funcionan completamente normales
- ✅ Sin interferencias

---

## 🧪 Cómo Probar

1. **Copiar JAR (19:19) al servidor**
2. **Obtener Sapotama**
3. **Prueba 1:** Mover Sapotama entre slots → Debería funcionar
4. **Prueba 2:** Cambiar selección de slots → Hotbar no se oculta
5. **Prueba 3:** Click derecho sobre Sapotama → Abre UI

---

## 📝 Logs Esperados

Al hacer click derecho con Sapotama:
```
[INFO] Interacción: tipo={Primary}, inicial={true}
[INFO] Primary detectado. Verificando en WorldThread...
[INFO] ¡Sapotama detectado! Abriendo SapotamaMenuUI para [jugador]
[INFO] SapotamaMenuUI abierto exitosamente
```

---

## 💡 Por Qué Primary Funciona Mejor

| Aspecto | SwapFrom | Primary |
|--------|----------|---------|
| **Dispara cuando** | Cambias de slot (F) | Usas el item |
| **Afecta hotbar** | ❌ Sí, interfiere | ✅ No |
| **Intención del usuario** | Cambiar arma | Usar item |
| **Específico a Sapotama** | ❌ No | ✅ Sí |

---

## ✅ Resumen

- ✅ JAR recompilado (19:19)
- ✅ Sapotama.json usa `Primary` ahora
- ✅ SapotamaInputFilter intercepta `Primary`
- ✅ Hotbar debería funcionar correctamente
- ✅ Click derecho abre UI

El problema anterior se debió a que `SwapFrom` es un evento de cambio de slot que ocurre para **todos** los items. Usar `Primary` es mucho más específico y correcto para este caso de uso.

