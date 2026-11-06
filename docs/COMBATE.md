# Sistema de combate de DigiTale — auditoría y rediseño

Este documento explica **qué estaba mal**, **qué se cambió** y **cómo ajustarlo**.
La referencia de diseño es el combate de *Digimon Story: Time Stranger*
(turnos por SPD, cambio sin coste de turno, puntos críticos/CP, guardia,
debilidades de hasta x3, analizar, artes cruzados), adaptado a Hytale.

> Estado: compila (`javac` y `./gradlew shadowJar`) y el motor se validó con
> simulaciones headless: 300 combates aleatorios (0 bloqueos), 40 combates con
> **control de los tres aliados** (39/40, 0 errores de cola), 60 encuentros con
> grupos de 3–4 simultáneos y prueba de SPD (3 vs 12 turnos del mismo aliado).

---

## 1. Resumen de lo implementado

| Área | Antes | Ahora |
|---|---|---|
| Turnos | El jugador actuaba cada ronda; las barras (gauge) nunca se reiniciaban → SPD decorativa | **ATB real**: las barras suben por SPD, quien llega a 1000 actúa y su barra vuelve a 0 |
| Lista de turnos | Mostraba el siguiente actor de una simulación aunque estuvieras decidiendo (parecía "turno del salvaje" mientras te pedía orden) | El primer slot es **quien actúa AHORA** (`AHORA`/`TÚ`/`ENEMIGO`); el resto es la cola |
| Cámara | `atan2(-dx, dz)` (signo de Z invertido) → cámara girada ~144°, escena fuera de cuadro | Pose 3/4 calculada con `Rotation3f.lookAt` del motor; se re-encuadra sola |
| Tipos | Solo elemento, máximo x1.5; el campo `atributo` (VACINE/VIRUS/DATA) no se usaba | Elemento **x** atributo hasta **x3.0**, con resistido (x0.5) e inmune (x0) |
| OP | Se gastaban pero casi no se ganaban; `EXE` (300) inalcanzable | Economía cerrada: +OP por turno/golpe/guardia/apoyo |
| CP | No existía | CP de equipo: se gana golpeando/debilidad/guardia; paga el **Arte Cruzado** |
| Guardia | Flag que nunca se limpiaba | Dura hasta tu próximo turno, reduce 60% y da CP |
| Cambio | Consumía turno y llamaba a `pasarTurno` dos veces | **No consume turno** y conserva tu turno |
| Analizar | No existía | Revela debilidades/resistencias, **no consume turno** |
| Ataque fuerte enemigo | `enemigoCargando` se ponía *después* de golpear | Telegrafía un turno y libera el siguiente (la guardia tiene sentido) |
| Estados | `SANGRANDO` eterno, dormidos logueaban "A/B/C", parálisis infinita | Contadores por turno propio, nombres reales, duraciones acotadas |
| Enemigos | 1 enemigo que se reemplazaba por otro al morir (oleadas) | **Grupo de 1–5 salvajes a la vez**, cada uno con su barra y su turno |
| Derrota | Penalizaba a todo el equipo (`d.hpCombate` obsoleto) | Solo los KO pierden stats; los KO vuelven con 25% de HP |
| XP | Se calculaba pero **nunca se aplicaba** | Se reparte a los vivos, sube `nivelAv` (desbloquea movimientos) y se persiste |
| Atributo/XP al guardar | `atributo` no se persistía; `nivelAv` de los slots C–F se perdía | Se guarda/lee y `repararIntegridad()` corrige partidas viejas |
| UI duplicada | `DigiBatallaMenuUI` (Page) muerta y `DigiBatallaComandosUI`+HUD vivos | Se eliminó la UI muerta; una sola ruta de comandos |
| Despawn | Reflexión probando enums y logueando en cada llamada | `store.removeEntity(ref, RemoveReason.REMOVE)` directo |
| Cámara ancla | Rol `CamaraAncla` roto (`MaxWalkSpeed = 0` → SEVERE al arrancar) | Se eliminó el NPC ancla y el rol |

---

## 2. La cámara: causa raíz del "no enfoca bien"

En el log del servidor se veía que la cámara caía siempre al modo de respaldo
(`custom_pose`) porque el NPC ancla no spawneaba (`Builder CamaraAncla failed
validation`). El cálculo era:

```java
double dx = tx - camX, dy = ty - camY, dz = tz - camZ;
float camYaw = (float) Math.atan2(-dx, dz);          // ❌
float camPitch = (float) Math.atan2(dy, Math.hypot(dx, dz));
```

La convención real de Hytale se puede leer en el propio motor
(`Rotation3f.getQuaternion` usa `Quaterniond.rotationYXZ(yaw, pitch, roll)`,
y `Rotation3f.lookAt` calcula `yaw = atan2(-dx, -dz)`). Es decir:

```
forward(yaw, pitch) = (-sin(yaw)·cos(pitch), sin(pitch), -cos(yaw)·cos(pitch))
```

Con los datos reales del log (`dx=3.5`, `dz=10.85`) el yaw correcto es
`atan2(-3.5, -10.85) ≈ -2.83 rad`, pero el código producía
`atan2(-3.5, 10.85) ≈ -0.31 rad`: **2,5 rad (≈144°) de error**. La escena
quedaba a un lado del cuadro (o directamente detrás de la cámara).

### Cámara nueva

- Pose absoluta `PositionType.Custom` + `RotationType.Custom` (sin NPC ancla).
- Se coloca **detrás del jugador**, a un hombro y elevada:
  `cam = jugador - forward·6.6 + right·2.0 + (0, suelo+3.3, 0)`.
- Apunta al **centro ponderado de la escena** (party + enemigo con peso 2),
  a `suelo + 0.8`, usando `Rotation3f.lookAt(dx, dy, dz)` — sin trigonometría
  propia, así nunca se vuelve a desincronizar.
- `FOV 70`, `position/rotationLerpSpeed 0.18`, cursor visible, sin retícula,
  `movementForceRotation` hacia el enemigo (aunque el jugador esté congelado).
- **Re-encuadre automático** cada 250 ms desde `EscenarioBatalla`; solo envía el
  paquete si la pose cambió más de 0.05 bloques / 0.008 rad.

### Cómo ajustarla en vivo

```
/digi_cam                    perfil por defecto
/digi_cam 8 1 1 3.8          distancia 8, altura 3.8
/digi_cam estado             imprime la pose calculada
/digi_cam 0 0 0              libera la cámara
```

Constantes en `CamaraCombate`: `DISTANCIA=6.6`, `LATERAL=2.0`, `ALTURA=3.3`,
`FOV=70`, `ALTURA_MIRA=0.8`. Si quieres una cámara más cenital, sube `ALTURA` o baja
`ALTURA_MIRA`; para un plano más cercano, baja `DISTANCIA`.

---

## 3. Reglas del combate nuevo

### 3.1 Turnos (ATB)

```
cada tick: gauge += SPD·12 + azar(0..11)
quien llega a GAUGE_MAX (1000) actúa y su gauge = 0
```

- El jugador controla a **los tres aliados** (modelo Time Stranger): en el
  turno de cada uno se abre el menú con **sus** movimientos, su OP y su objetivo.
  El HUD marca `▶ TÚ` al que está actuando. Todos los salvajes son IA enemiga.
- Cada salvaje del grupo tiene su propia barra: pueden actuar antes o después
  que los tuyos y varias veces por ronda si su SPD es alto.
- Al empezar hay **iniciativa del jugador** (equivalente al *DigiAttack*
  preventivo de Time Stranger): el jugador actúa primero y luego corre el ATB.
- Si el aliado del turno está dormido/aturdido/paralizado, el turno se pierde
  solo y corre el siguiente actor.
- La "línea de turnos" usa `SistemaBatalla.predecirTurnos`, que simula el ATB
  sobre una copia. **El primer slot es siempre quien actúa AHORA** (marcado
  `AHORA` si te toca a ti) y el resto es la cola prevista, con la etiqueta
  `TÚ` / `ENEMIGO` para que no se confunda con quien está peleando.
- La línea está **animada** desde el servidor (`DigiBatallaComandosUI`, 8 fps):
  la fila activa pulsa (`#TurnPulse`), el estado muestra `AHORA` con puntos
  suspensivos, las barras se rellenan interpolando entre el valor anterior y el
  nuevo, y las filas entran deslizándose cuando cambia la cola. Hytale no tiene
  animaciones declarativas en la UI (`CustomUICommand` solo hace Set/Append), por
  eso se anima con actualizaciones periódicas.

### 3.2 Economía de OP y CP

| Acción | OP | CP |
|---|---|---|
| Empezar tu turno | +8 (empiezas con 50) | — |
| Golpear | +12 | +6 (+10 si es punto débil, +5 si crítico) |
| Recibir daño | +8 | +3 |
| Guardia | +20 | +8 |
| Apoyo (concentrarse) | +25 a todos | +25 |
| Analizar | — | +5 |

- **OP** (0–200) paga movimientos; cada Digimon tiene los suyos (empieza con 50).
- Los multiplicadores/costes de las 7 técnicas van en orden creciente
  (`1.2/20`, `1.6/40`, `2.0/60`, `2.4/90`, `3.0/110`, `3.6/130`, `4.2/150`).
- **CP** (0–100) es del equipo y paga el **Arte Cruzado** (100 CP).
- Los costes de los movimientos vienen de `DatoDigimon.Movimiento.coste`.

### 3.3 Comandos del jugador

| Comando | ¿Consume turno? | Efecto |
|---|---|---|
| **ATACAR** | Sí | Ataque básico gratis (1.1x) y **después eliges el objetivo** |
| **HABILIDAD** | Sí | Eliges el movimiento y **después el objetivo** |
| **APOYO → PROTEGER** | Sí | Guardia: −60% daño hasta tu próximo turno |
| **APOYO → CONCENTRAR** | Sí | +25 OP a todos, +25 CP, cura 5% |
| **APOYO → ANALIZAR** | **No** | Revela debilidades/resistencias del objetivo |
| **APOYO → ARTE CRUZADO** | **No** | Todos los vivos golpean a la vez (100 CP) |
| **CAMBIAR** | **No** | Sustituye al **miembro que tiene el turno** por una reserva; el que entra actúa ya |
| **HUIR** | Sí (si falla, el enemigo golpea) | Prob. según SPD relativa |

### 3.4 Tabla de tipos

Dos ejes que se **multiplican** (tope x3.0):

**Elemento** (atacante → defensor): FUEGO/NATURALEZA/HIELO, AGUA/FUEGO/TIERRA,
NATURALEZA/AGUA/TIERRA, HIELO/NATURALEZA/VIENTO, TRUENO/AGUA/VIENTO/METAL,
VIENTO/NATURALEZA/TRUENO, LUZ↔OSCURIDAD, TIERRA/TRUENO/FUEGO/METAL,
METAL/HIELO/LUZ. Cada ventaja x1.5, cada resistencia x0.5.

**Atributo** (triángulo clásico): `VACCINE > VIRUS > DATA > VACCINE`
(x2.0 a favor, x0.5 en contra). `FREE`/`UNKNOWN` son neutrales.

Ejemplos:
- FUEGO/VACCINE contra NATURALEZA/VIRUS → `1.5 × 2.0 = 3.0` (**punto crítico**).
- FUEGO/VACCINE contra AGUA/VACCINE → `0.5 × 1.0 = 0.5` (resistido).
- LUZ/VACCINE contra OSCURIDAD/VIRUS → `1.5 × 2.0 = 3.0`.

Todo está en `TablaTipos` (única fuente de verdad; `EnemigoCombate.multElemental`
delega ahí). El atributo de enemigos se resuelve por nombre en
`TablaTipos.atributoDeEnemigo` y el de compañeros con
`TablaTipos.atributoDeEspecie` (también usado para reparar partidas viejas).

### 3.4b Fórmula de daño

```
daño = max( 1, ATK × multiplicador − 0.35 × DEF )
     × ventaja de tipo (hasta x3)
     × (crítico 1.4 | muy crítico 1.8)
     × variación 0.9..1.1
suelo = maxHP_del_objetivo / 20      (evita peleas eternas a nivel 1)
```

- El suelo se aplica a los golpes normales/críticos, **no** a los resistidos
  (que hacen 1) ni a los inmunes (0).
- En los ataques enemigos el suelo se aplica **antes** de la guardia, para que
  PROTEGER siga reduciendo de verdad (x0.4).

### 3.5 Estados alterados

Se aplican al golpear solo si el movimiento los define (`Movimiento.efecto`) y
se descuentan **al inicio del turno del afectado**, con su nombre real en el log:

- 🔥 QUEMADO: 3% HP/turno, 3 turnos.
- ☠ VENENO / ENVENENADO_FUERTE: 3% / 6% HP/turno, 4 turnos.
- 🩸 SANGRANDO: 4% HP/turno, 3 turnos.
- 💤 DORMIDO: pierde turnos; despierta al llegar a 0 o 30% por turno.
- 😵 ATURDIDO: pierde el turno indicado.
- ⚡ PARALISIS: 25% de perder el turno, dura 2–3 turnos.

### 3.6 Grupo enemigo (1–5 a la vez)

- El encuentro genera un **grupo completo** (`EnemigoCombate.generarGrupo`): el
  primero es la especie del salvaje que golpeaste y el resto sale del mismo
  escalón (3 Rookies, 3 Champions…) para que el grupo sea coherente.
- **Todos aparecen al mismo tiempo** en formación (`SistemaPaseo.posicionEnemigo`):
  1 al centro; 2 en línea; 3–5 en una o dos filas. Cada uno tiene su barra, sus
  estados y su turno. Al morir uno **no aparece otro**: la pelea sigue con los
  que quedan hasta que caen todos.
- Objetivo: el motor autoelige el enemigo al que más le pega tu elemento/atributo
  y, a igualdad, el de menos HP. Puedes cambiarlo gratis con
  **APOYO → OBJETIVO** (se marca con `▶` en el HUD).
- Cada 4 turnos enemigos, **el salvaje que actúa** carga (telegrafiado ⚠) y a su
  siguiente turno libera un golpe x2.1: ahí es donde la guardia importa.
- Al ganar, todos los salvajes del grupo se retiran del mundo; si huyes, solo se
  retiran los que spawneó la batalla y los originales se quedan.

### 3.6a Atributos: Baby / In-Training = SIN ATRIBUTO

Según Wikimon, la clasificación **None** (sin atributo) se aplica a los Digimon
**Baby I (Fresh)** y **Baby II (In-Training)**, que todavía no desarrollan
atributo; Vaccin/Data/Virus empiezan en Child/Rookie. Ejemplo canónico: Botamon
(Baby I) no tiene atributo.

En el mod:

- `crearInicial` fuerza `NONE` para nivel 1–2 (Baby e In-Training). Calumon,
  NEO y Burpmon conservan `UNKNOWN` (canon).
- `repararIntegridad` migra partidas antiguas: si el Digimon es nivel 1–2 y
  tenía `DATA`/`VIRUS`/`VACCINE` guardado, pasa a `NONE` (también al cargar el
  banco).
- `atributoDeEspecie` reconoce la lista de Baby/In-Training y devuelve `NONE`.
- Los enemigos salvajes de escalón 1 (Botamon/Punimon/Nyokimon Salvaje) también
  son `NONE`.
- En combate `NONE`, `FREE` y `UNKNOWN` son **neutrales** en el triángulo de
  atributos (multiplicador 1.0), así que no rompen el daño.
- La UI lo muestra como `SIN ATRIBUTO` y con el icono `None.png`.

Referencia: [Wikimon — Attribute](https://wikimon-net.translate.goog/Attribute?_x_tr_sl=en&_x_tr_tl=es&_x_tr_hl=es&_x_tr_pto=tc)

### 3.6b Animaciones y cámara de combate

- **AI congelada durante el combate**: a todos los NPC (party y salvajes) se
  les añade el componente `Frozen` del motor. El `BehaviourTickSystem` de Hytale
  salta el comportamiento de los NPC congelados, así que **ni se mueven ni giran**
  mientras se reproduce un ataque o reciben daño, y la cámara no se queda
  enfocando a un modelo que se fue. Al salir del combate se descongelan (el
  salvaje original recupera su comportamiento si huyes).
- **Idle estable**: al entrar en combate los Digimon se quedan quietos. Se
  fuerza el estado `Idle` del NPC y se reproduce su animación `Idle` (una vez
  tras la entrada, luego cada ~650 ms) para que dejen de saltar/alertarse.
  Además la formación ya no re-teleporta a los NPCs cada tick (eso los hacía
  creerse en el aire y repetir el salto) y quedan apoyados en el suelo.
- **Golpe cinematográfico (solo tu equipo)**: cada ataque registra un evento
  visual (`EstadoCombate.EventoVisual`). `AnimacionesCombate` lo reproduce así:
  1. Enfoca al **atacante** y le reproduce `Attack` (~700 ms).
  2. Enfoca al **objetivo** y reproduce `Hurt` o `Death` (~950/1600 ms).
  3. Vuelve al plano general.
- **Turnos enemigos también con plano**: se enfoca al enemigo que ataca y al
  objetivo, con tiempos medios (~550/750 ms / 1300 ms si muere) para que se
  vea la acción sin que pase de golpe al siguiente turno.
- **Orientación**: el atacante gira hacia su objetivo al empezar su animación y
  el defensor gira hacia el atacante al recibirla; en reposo cada Digimon mira
  al centro del grupo rival. Así el modelo siempre encara lo que golpea.
- **Preparación y respiro**: antes del foco hay 300 ms de plano general
  (`PRE_ROLL_MS`), la cámara tiene 240 ms para llegar al atacante
  (`FASE_LLEGADA_ATAQUE_MS`) y 280 ms para llegar al objetivo
  (`FASE_LLEGADA_DANO_MS`) **antes** de reproducir `Attack`/`Hurt`; después de
  cada golpe hay 260 ms (`PAUSA_ENTRE_EVENTOS_MS`) antes de resolver al
  siguiente actor. Así el `Hurt` no se reproduce con la cámara todavía en
  camino.
- **Partículas nativas**: al impactar se lanza un sistema de partículas del
  juego según el elemento (`Impact_Fire`, `Impact_Ice`, `VoidImpact`,
  `Impact_Poison`, `Impact_Critical`, `Impact_Sword_Basic`); las muertes
  añaden `Impact_Critical` y las acciones del jugador llevan un `SyncAura` en
  el atacante. Son efectos del propio Hytale, sin assets extra.
- La cámara de foco usa `position/rotationLerpSpeed = 0.20` (deslizamiento
  suave) y el foco se mantiene más tiempo antes del siguiente plano.
- **La cámara escala con el tamaño de los Digimon**: `SistemaPaseo` calcula la
  altura media (hitbox) de party + enemigos y la guarda en
  `EstadoCombate.alturaMedia`; `CamaraCombate` usa
  `k = clamp(alturaMedia / 2.0, 0.5, 3.0)` para distancia, lateral, altura y
  punto de mira, tanto en el plano general como en el foco. Bebés (~0.25–0.5)
  quedan mucho más cerca; Adultos (~2) mantienen el encuadre actual; Megas y
  colosos se alejan solos. La media se recalcula al cambiar de Digimon.
- La cámara de foco (`CamaraCombate.enfocar`) se coloca en el terreno intermedio
  entre party y enemigos, a un lado y a 2,1 de altura, con FOV 60.
- Si el modelo no tiene la animación (`Attack`/`Hurt`/`Death`), se prueban
  alias (`Shoot`, `Hit`, `Die`…) y, si no hay, el foco se hace igual.
- Mientras se reproduce, `cinematicaActiva` **bloquea el menú**: los botones se
  **ocultan** (no parecen colgados), el panel dice `ANIMANDO COMBATE` y al
  terminar se refresca la página con el nuevo turno, el registro y los botones.
- Un salvaje derrotado permanece ~1,1 s para verse la animación de muerte y
  después se retira de la escena.
- Elige el objetivo con **APOYO → OBJETIVO**: lista de salvajes vivos con su
  HP y elemento; el elegido se marca con `▶`.

### 3.7 Victoria, derrota y XP

- **El turno avanza solo al terminar la animación**: el motor resuelve **un
  actor por llamada** y es `AnimacionesCombate` quien lo vuelve a llamar cuando
  acaba el golpe. Así el siguiente turno (y el cambio en la línea de turnos) no
  ocurre hasta después de las animaciones. Si la presentación no estuviera viva,
  el lock de la escena tiene un fallback que avanza el combate.
- **Victoria**: XP de todo el grupo + bonus de combo (`comboMax × 5`). Cada vivo sube
  `xp` y, cada 100, un nivel de aventura (desbloquea movimientos). Se guarda
  `nivelAv`/`xp` en el componente persistente (antes se perdía en los slots C–F).
- Los KO vuelven con 25% de HP; el equipo conserva el HP de combate.
- **Derrota** (no PvP): −20% stats a los **caídos** (−10% con Calumon).

### 3.8 Preguntas frecuentes (las dudas del playtest)

- **¿Por qué un compañero gana OP y no baja de HP?** Cada aliado gana `+8 OP`
  al empezar **su** turno y solo pierde HP si un salvaje le pega. El enemigo
  elige objetivo 50% al más herido y 50% al azar, así que el daño se reparte.
  El HUD marca `▶ TÚ` al que tiene el turno.
- **¿Por qué un aliado actúa dos veces seguidas?** Su SPD es mayor: el ATB
  se lo permite y la cola lo muestra con el mismo nombre dos veces.
- **¿Por qué el juego dice "Espera tu turno"?** Porque el actor actual es otro
  (salvaje). La cola marca `AHORA` al que está actuando; si eres tú, el banner
  dice `TU TURNO · <nombre> (A/B/C)`.
- **¿Por qué al matar un salvaje ya no sale otro?** Porque ya no hay oleadas: el
  grupo entero (1–5) aparece al inicio y la pelea termina cuando caen todos.
- **¿Por qué el turno no cambia en la línea mientras se ve el golpe?** Porque el
  motor resuelve **un actor por vez** y es la capa de animación la que lo vuelve
  a llamar al terminar la animación. El siguiente turno no empieza hasta que se
  ha visto el golpe anterior (incluido el del enemigo).
- **¿Cómo elijo el objetivo?** Primero ATACAR o la habilidad; después aparece la
  lista de salvajes vivos con su HP, elemento y multiplicador de tipo. `VOLVER`
  regresa a la lista de habilidades.
- **¿Por qué ya no se mueven/giran durante el ataque?** Porque el combate
  congela la AI de los NPC con `Frozen`; las animaciones se siguen enviando, pero
  el modelo se queda en su sitio. Al salir se descongela.
- **¿Por qué durante la animación no puedo pulsar?** Porque el turno todavía no
  ha avanzado: la escena reproduce el golpe antes de resolver al siguiente
  actor. Los botones se ocultan mientras dura y reaparecen en tu turno.
- **¿Los enemigos tienen plano de cámara?** Sí: al atacar se enfoca al enemigo
  y luego al objetivo, con tiempos medios para que se vea la acción. Tus
  acciones usan planos algo más largos (~700/950 ms).

---

## 4. Archivos tocados

| Archivo | Cambio |
|---|---|
| `datos/TablaTipos.java` | **Nuevo**: elementos + atributos + análisis |
| `datos/EstadoCombate.java` | Turnos ATB, CP, guardia, contadores, helpers por `Lado` |
| `datos/EnemigoCombate.java` | Atributo, generación por especie, escalado, rol salvaje |
| `datos/DatoDigimon.java` | `xp`, `subirNivelAventura()`, `repararIntegridad()` |
| `datos/AlmacenJugadores.java` | `refsBatallaEspecie` (modelo correcto tras Cambio) |
| `sistema/SistemaBatalla.java` | Motor ATB, grupo enemigo, objetivo, daño, IA, CP, XP |
| `sistema/CamaraCombate.java` | Encuadre general + planos de foco de ataque/daño |
| `sistema/AnimacionesCombate.java` | **Nuevo**: Idle, Attack/Hurt/Death y cinemática |
| `sistema/EscenarioBatalla.java` | Escena, formación del grupo, lock idempotente |
| `sistema/SistemaPaseo.java` | Formación de party + grupo, spawn de salvajes, despawn |
| `ui/MenuBatalla.java` | Menú nuevo (APOYO), turnos, CP, cambio sin turno |
| `ui/DigiBattleHud.java` | CP, tipo/debilidades, turno, estados, guardia |
| `ui/DigiBatallaComandosUI.java` | CP, objetivo, línea de turnos animada y panel de registro |
| `ui/LogCombateUI.java` | **Nuevo**: registro con auto-scroll a las últimas líneas |
| `comandos/DigiCamComando.java` | Ajuste de cámara en vivo |
| `comandos/DigiStatusComando.java` | Atributo, XP y estado de combate real |
| `ui/DigiBatallaMenuUI.java` | **Eliminado** (UI muerta duplicada) |
| `Server/NPC/Roles/CamaraAncla.json` | **Eliminado** (rol roto y ya innecesario) |
| `componentes/PetProgressComponent.java` | Persistencia de `nivelAv`/`xp` + reparación |

---

## 5. Cómo probar

1. Compilar: `./gradlew shadowJar` (o `javac -cp libs/HytaleServer.jar:Vuetale-1.0.30.jar`).
2. Copiar `build/libs/DigiTaleOficial-1.0-SNAPSHOT.jar` a `server/plugins/`.
3. En el juego: golpear un salvaje con el Sapotama.
4. Comprobar:
   - La cámara queda detrás del jugador, con la party abajo y el enemigo al fondo.
   - `TURNO 1 · CP 0` y `TU TURNO · <nombre>` en el HUD.
   - La lista de salvajes muestra 1–5 filas con su HP; **todos aparecen a la
     vez** en la escena y al matar uno **no** aparece otro (los caídos
     desaparecen y la cámara se reajusta).
   - La línea de turnos marca `AHORA` al aliado que te toca (A/B/C) y los
     salvajes como `ENEMIGO`; el título del menú dice de quién es el turno.
   - La línea de turnos está **animada**: la fila activa pulsa (barra cian a la
     izquierda) y las barras muestran el gauge real (estático).
   - El **registro de combate** está en el panel inferior derecho (ya no debajo
     de los botones): muestra siempre las **últimas 10 líneas**, con la más
     reciente abajo y marcada con `»` (auto-scroll al nuevo mensaje).
   - Al elegir ATACAR/HABILIDAD aparece la lista de objetivos; al confirmar, la
     cámara hace un plano del atacante y luego del objetivo con `Attack` y
     `Hurt`/`Death`; el menú se bloquea con `ANIMANDO COMBATE` hasta que termina.
   - La línea de turnos **no avanza** hasta que acaba la animación del golpe.
   - Elegir ATACAR/HABILIDAD abre la lista de objetivos; al confirmar, la
     animación y el cambio de turno van en orden.
   - APOYO → ANALIZAR muestra "DÉBIL a … / RESISTE …" y APOYO → OBJETIVO cambia
     el `▶` de salvaje (gratis).
   - CAMBIAR no gasta el turno (puedes atacar con el que entra).
   - Al 4.º turno enemigo aparece `⚠CARGANDO`; al siguiente golpea fuerte:
     si usaste PROTEGER, el log dice `(BLOQUEADO)`.
   - Con 100 CP, APOYO → ARTE CRUZADO ataca con todo el equipo y no gasta turno.
   - `/digi_cam estado` imprime la pose; `/digi_cam 8 1 1 4` la aleja y eleva.

### Simulación headless

`SimCombate` / `Sim2` / `Sim6` (en `/tmp`, no forman parte del repo) ejercitaron:
300 combates aleatorios, SPD 4 vs 20, arte cruzado, guardia, cambio, derrota
forzada, 40 combates con **control de los tres aliados** y 60 encuentros con
**grupos de 3–4 simultáneos**. Resultados: 0 bloqueos, 39/40 con los tres
controlados, 0 errores de cola (el primer slot siempre es el actor actual),
60/60 en grupos del mismo escalón, cambio sin consumir turno verificado, SPD 4 →
3 turnos vs SPD 20 → 12, multiplicadores x3.0/x0.5 correctos.

---

## 6. Pendientes recomendados (siguiente iteración)

1. **PvP**: el motor ya distingue `esPvP` y no penaliza; falta emparejar dos
   `EstadoCombate` y sincronizar turnos entre dos jugadores.
2. **Ítems en combate** (curar/curare estados) sin consumir turno, 1 por turno
   (regla de Time Stranger).
3. **Scan rate / reclutamiento**: `SistemaIncubacion.bebeDe` ya mapea especie →
   bebé; se puede acumular "scan" al ganar y convertir.
4. **Habilidades por especie**: hoy los movimientos son genéricos por nivel;
   conviene una tabla `Especie → movimientos` (con `efecto` y elemento propio).
5. **Animaciones/feedback**: la cámara podría hacer un `zoom` corto al que actúa
   (usar `CameraKeyframe`/`CameraSequenceBuilder`) y un shake al crítico.
6. **Balance**: los Baby salvajes se bajaron a 15–18 HP para que el inicio no
   sea eterno. Ajustar `tamanoEncuentro`, las tablas de stats y el escalado por
   zona (`EnemigoCombate.crear`). Con los tres aliados controlados el jugador
   tiene más margen, así que se puede subir un poco la dificultad.
   Los enemigos fuera de la tabla usan una curva por nivel de zona
   (`EnemigoCombate.sintetico`): si quieres stats exactos por especie, añade una
   tabla `Especie → stats` o deduce la etapa por rol.
7. **Borrar legado**: `DatosJugador.enCombate*`, `terminarCombate()`,
   `iniciarCombate(...)`, `tacticaA/B/C` y el enum `Lado` de `EstadoCombate` ya
   no los usa el motor; se pueden eliminar cuando no queden referencias.
8. **Autobatalla opcional**: al controlar los tres aliados, un botón
   "AUTO" que resuelva los turnos aliados con IA (como antes) sería útil para
   farmear; el motor ya distingue cada actor y bastaría no marcar
   `esperandoJugador` para los aliados en modo automático.
