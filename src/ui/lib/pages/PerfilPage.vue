<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, captionStyle, label, panelBg, rowBg } from '@/cyber'

interface StatData {
  vacio: boolean
  nombre: string
  especie: string
  nivel: string
  elemento: string
  atributo: string
  hp: string
  atk: string
  def: string
  spd: string
  wis: string
  lazo: string
  disciplina: string
  victorias: string
  derrotas: string
  energia: string
  hambre: string
  fuerte: string
  debil: string
  imagen: string
  iconoElemento: string | null
  iconoAtributo: string | null
}

interface MovimientoData {
  index: number
  nombre: string
  categoria: string
  poder: number
  golpes: number
  critico: number
  alcance: string
  coste: number
  descripcion: string
}

interface EvolucionOpcion {
  destino: string
  etapa: string
  disponible: boolean
  condiciones: string
}

interface EvolucionData {
  actual: string
  condiciones: string
  info: string
  opciones: EvolucionOpcion[]
}

interface DeEvolucionOpcion {
  destino: string
  disponible: boolean
}

interface DeEvolucionData {
  actual: string
  info: string
  opciones: DeEvolucionOpcion[]
}

interface EquipamientoData {
  equipo: string
  arma: string
  armadura: string
  cosmetico1: string
  cosmetico2: string
}

type TabId = 'stats' | 'movimientos' | 'evolucion' | 'de_evolucion' | 'equipamiento'

const titulo = useData<string>('titulo', 'PERFIL DEL DIGIMON')
const pestana = useData<TabId>('pestana', 'stats')
const stats = useData<StatData>('stats', {
  vacio: true,
  nombre: '-',
  especie: '-',
  nivel: '-',
  elemento: '-',
  atributo: '-',
  hp: '- / -',
  atk: '-',
  def: '-',
  spd: '-',
  wis: '-',
  lazo: '- / 100',
  disciplina: '-',
  victorias: '-',
  derrotas: '-',
  energia: '- / 100',
  hambre: '- / 100',
  fuerte: '-',
  debil: '-',
  imagen: '',
  iconoElemento: null,
  iconoAtributo: null,
})
const movimientos = useData<MovimientoData[]>('movimientos', [])
const movimientoSeleccionado = useData<number>('movimientoSeleccionado', 0)
const evolucion = useData<EvolucionData>('evolucion', {
  actual: '-',
  condiciones: '',
  info: '',
  opciones: [],
})
const deEvolucion = useData<DeEvolucionData>('deEvolucion', {
  actual: '-',
  info: '',
  opciones: [],
})
const equipamiento = useData<EquipamientoData>('equipamiento', {
  equipo: 'Sin equipo',
  arma: 'Sin arma',
  armadura: 'Sin armadura',
  cosmetico1: 'Aspecto Normal',
  cosmetico2: 'Aspecto Especial',
})
const sendAction = useData<(payload: string) => void>('onAction', () => {})
const animarEntrada = useData<boolean>('animarEntrada', true)

const tabs: { id: TabId; label: string }[] = [
  { id: 'stats', label: 'STATS' },
  { id: 'movimientos', label: 'MOVIM.' },
  { id: 'evolucion', label: 'EVOL.' },
  { id: 'de_evolucion', label: 'DEEVO.' },
  { id: 'equipamiento', label: 'EQUIPO' },
]

const filasMov = computed<MovimientoData[][]>(() => {
  const out: MovimientoData[][] = []
  const data = movimientos.value
  for (let i = 0; i < data.length; i += 2) {
    out.push(data.slice(i, i + 2))
  }
  return out
})

const movSeleccionado = computed(() =>
  movimientos.value.find((m) => m.index === movimientoSeleccionado.value),
)

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

function navegar(id: 'equipo' | 'crianza' | 'batalla' | 'perfil') {
  if (id === 'equipo') {
    act('volver')
  } else if (id === 'crianza') {
    act('ir_crianza')
  } else if (id === 'batalla') {
    act('ir_batalla')
  }
}

const sectionTitleStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 11, 'Start', true, { LetterSpacing: 1 }),
)
const mutedStyle = computed<LabelStyle>(() => label(CYBER.muted, 10, 'Start', false, { Wrap: true }))
const labelStyle = computed<LabelStyle>(() => label(CYBER.cyan, 10, 'Start', true, { LetterSpacing: 1 }))
const valueStyle = computed<LabelStyle>(() => label(CYBER.text, 11, 'Start'))
const bigValueStyle = computed<LabelStyle>(() => label(CYBER.text, 14, 'Start', true))
const greenValueStyle = computed<LabelStyle>(() => label(CYBER.green, 11, 'Start'))
const magentaValueStyle = computed<LabelStyle>(() => label(CYBER.magenta, 11, 'Start'))
const goldValueStyle = computed<LabelStyle>(() => label(CYBER.gold, 11, 'Start'))
const orangeValueStyle = computed<LabelStyle>(() => label('#ff8a5c', 11, 'Start'))
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))

const tabStyle: TextButtonStyle = buttonStyle(CYBER.muted)
const tabActiveStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_active')
const moveButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const moveActiveStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_active')
const disabledButtonStyle: TextButtonStyle = buttonStyle(CYBER.muted, 'panel_soft')
const backButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
</script>

<template>
  <CyberShell
    :animate="animarEntrada"
    title="DIGIMON ANALYZER"
    :subtitle="'PERFIL // ' + stats.nombre"
    active="perfil"
    :status="'NIVEL ' + stats.nivel"
    @nav="navegar"
  >
    <Group layout-mode="Left" :anchor="{ Height: 34 }">
      <!-- IMPORTANTE: el :key incluye el estado activo. Si solo cambiara el
           :el-style, Vuetale actualizaria el Background del boton ya renderizado
           y el cliente de Hytale se congela (limitacion conocida de Vuetale). -->
      <template
        v-for="(t, i) in tabs"
        :key="t.id + (pestana === t.id ? '-on' : '-off')"
      >
        <TextButton
          :text="(pestana === t.id ? '> ' : '') + t.label"
          :anchor="{ Width: 140, Height: 34 }"
          :el-style="pestana === t.id ? tabActiveStyle : tabStyle"
          @activating="act('tab', t.id)"
        />
        <Group v-if="i < tabs.length - 1" :anchor="{ Width: 6 }" />
      </template>
    </Group>

    <Group :anchor="{ Height: 6 }" />

    <Group
      layout-mode="Top"
      :anchor="{ Height: 400 }"
      :background="panelBg"
      :padding="{ Full: 10 }"
    >
      <template v-if="pestana === 'stats'">
        <Group layout-mode="Left" :anchor="{ Height: 124 }">
          <AssetImage
            :asset-path="stats.imagen || 'UI/Custom/png_Digis/Botamon.png'"
            :visible="!stats.vacio && !!stats.imagen"
            :anchor="{ Width: 112, Height: 112 }"
          />
          <Group :anchor="{ Width: 10 }" />
          <Group layout-mode="Top" :anchor="{ Width: 640, Height: 118 }">
            <Label :text="stats.nombre" :el-style="bigValueStyle" :anchor="{ Height: 26 }" />
            <Label :text="stats.especie + '  //  ' + stats.nivel" :el-style="mutedStyle" :anchor="{ Height: 18 }" />
            <Group :anchor="{ Height: 4 }" />
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="ELEM" :el-style="labelStyle" :anchor="{ Width: 46 }" />
              <AssetImage
                :asset-path="stats.iconoElemento || 'UI/Textures/Icons/Elemental/Neutral.png'"
                :visible="!!stats.iconoElemento"
                :anchor="{ Width: 18, Height: 18 }"
              />
              <Group :anchor="{ Width: 4 }" />
              <Label :text="stats.elemento" :el-style="valueStyle" :anchor="{ Height: 20 }" />
              <Group :anchor="{ Width: 18 }" />
              <Label text="TIPO" :el-style="labelStyle" :anchor="{ Width: 40 }" />
              <AssetImage
                :asset-path="stats.iconoAtributo || 'UI/Textures/Icons/Type/None.png'"
                :visible="!!stats.iconoAtributo"
                :anchor="{ Width: 18, Height: 18 }"
              />
              <Group :anchor="{ Width: 4 }" />
              <Label :text="stats.atributo" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="HP" :el-style="labelStyle" :anchor="{ Width: 46 }" />
              <Label :text="stats.hp" :el-style="greenValueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="LAZO" :el-style="labelStyle" :anchor="{ Width: 46 }" />
              <Label :text="stats.lazo" :el-style="magentaValueStyle" :anchor="{ Height: 20 }" />
            </Group>
          </Group>
        </Group>

        <Group :anchor="{ Height: 4 }" />
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
        <Group :anchor="{ Height: 6 }" />

        <Group layout-mode="Left" :anchor="{ Height: 82 }">
          <Group layout-mode="Top" :anchor="{ Width: 238, Height: 82 }">
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="ATK" :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.atk" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="DEF" :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.def" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="SPD" :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.spd" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="WIS" :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.wis" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
          </Group>
          <Group :anchor="{ Width: 12 }" />
          <Group layout-mode="Top" :anchor="{ Width: 238, Height: 82 }">
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="DISC." :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.disciplina" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="VICT." :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.victorias" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="DERR." :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.derrotas" :el-style="valueStyle" :anchor="{ Height: 20 }" />
            </Group>
            <Group layout-mode="Left" :anchor="{ Height: 20 }">
              <Label text="ENERGIA" :el-style="labelStyle" :anchor="{ Width: 70 }" />
              <Label :text="stats.energia" :el-style="goldValueStyle" :anchor="{ Height: 20 }" />
            </Group>
          </Group>
        </Group>

        <Group :anchor="{ Height: 4 }" />
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.magenta }" />
        <Group :anchor="{ Height: 6 }" />

        <Group layout-mode="Left" :anchor="{ Height: 22 }">
          <Label text="HAMBRE" :el-style="labelStyle" :anchor="{ Width: 90 }" />
          <Label :text="stats.hambre" :el-style="orangeValueStyle" :anchor="{ Width: 150 }" />
          <Label text="FUERTE VS" :el-style="greenValueStyle" :anchor="{ Width: 90 }" />
          <Label :text="stats.fuerte" :el-style="valueStyle" :anchor="{ Width: 150 }" />
        </Group>
        <Group layout-mode="Left" :anchor="{ Height: 22 }">
          <Label text="ESTADO" :el-style="labelStyle" :anchor="{ Width: 90 }" />
          <Label text="OPERATIVO" :el-style="greenValueStyle" :anchor="{ Width: 150 }" />
          <Label text="DEBIL VS" :el-style="orangeValueStyle" :anchor="{ Width: 90 }" />
          <Label :text="stats.debil" :el-style="valueStyle" :anchor="{ Width: 150 }" />
        </Group>

        <Group :anchor="{ Height: 10 }" />
        <Group layout-mode="Left" :anchor="{ Height: 34 }">
          <TextButton
            text="VOLVER AL EQUIPO"
            :anchor="{ Width: 200, Height: 34 }"
            :el-style="backButtonStyle"
            @activating="act('volver')"
          />
          <Group :anchor="{ Width: 8 }" />
          <Label :text="'ANALISIS // ' + titulo" :el-style="captionStyle(CYBER.muted)" :anchor="{ Width: 360, Height: 34 }" />
        </Group>
      </template>

      <template v-else-if="pestana === 'movimientos'">
        <Group layout-mode="Left" :anchor="{ Height: 22 }">
          <Label text="MOVIMIENTOS / TECNICAS" :el-style="sectionTitleStyle" :anchor="{ Width: 360 }" />
          <Label text="SELECCIONA UNO" :el-style="hintStyle" :anchor="{ Width: 128 }" />
        </Group>
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
        <Group :anchor="{ Height: 6 }" />

        <Group
          v-for="(fila, fi) in filasMov"
          :key="fi"
          layout-mode="Left"
          :anchor="{ Height: 44 }"
        >
          <TextButton
            v-for="m in fila"
            :key="m.index + (movimientoSeleccionado === m.index ? '-on' : '-off')"
            :text="m.nombre"
            :anchor="{ Width: 380, Height: 36 }"
            :el-style="movimientoSeleccionado === m.index ? moveActiveStyle : moveButtonStyle"
            @activating="act('seleccionar_movimiento', m.index)"
          />
          <Group :anchor="{ Width: 8 }" />
        </Group>

        <Group :anchor="{ Height: 8 }" />
        <Group
          v-if="movSeleccionado"
          layout-mode="Top"
          :anchor="{ Height: 172 }"
          :background="rowBg"
          :padding="{ Full: 8 }"
        >
          <Label
            :text="'SELECCIONADO // ' + movSeleccionado.nombre"
            :el-style="sectionTitleStyle"
            :anchor="{ Height: 18 }"
          />
          <Group layout-mode="Left" :anchor="{ Height: 20 }">
            <Label text="PODER" :el-style="labelStyle" :anchor="{ Width: 80 }" />
            <Label :text="String(movSeleccionado.poder)" :el-style="valueStyle" :anchor="{ Width: 120 }" />
            <Label text="GOLPES" :el-style="labelStyle" :anchor="{ Width: 80 }" />
            <Label :text="String(movSeleccionado.golpes)" :el-style="valueStyle" :anchor="{ Width: 120 }" />
          </Group>
          <Group layout-mode="Left" :anchor="{ Height: 20 }">
            <Label text="CRITICO" :el-style="labelStyle" :anchor="{ Width: 80 }" />
            <Label :text="String(movSeleccionado.critico)" :el-style="valueStyle" :anchor="{ Width: 120 }" />
            <Label text="ALCANCE" :el-style="labelStyle" :anchor="{ Width: 80 }" />
            <Label :text="movSeleccionado.alcance" :el-style="valueStyle" :anchor="{ Width: 120 }" />
          </Group>
          <Group :anchor="{ Height: 4 }" />
          <Label :text="movSeleccionado.descripcion" :el-style="mutedStyle" :anchor="{ Height: 56 }" />
        </Group>
        <Label
          v-else
          text="Selecciona un movimiento para ver detalles."
          :el-style="mutedStyle"
          :anchor="{ Height: 24 }"
        />
      </template>

      <template v-else-if="pestana === 'evolucion'">
        <Group layout-mode="Left" :anchor="{ Height: 22 }">
          <Label text="EVOLUCION" :el-style="sectionTitleStyle" :anchor="{ Width: 360 }" />
          <Label :text="evolucion.actual" :el-style="valueStyle" :anchor="{ Width: 128 }" />
        </Group>
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.magenta }" />
        <Group :anchor="{ Height: 6 }" />

        <template v-if="evolucion.opciones.length > 0">
          <template v-for="o in evolucion.opciones" :key="o.destino">
            <Group layout-mode="Left" :anchor="{ Height: 44 }">
              <TextButton
                :key="o.destino + (o.disponible ? '-on' : '-off')"
                :text="o.destino + (o.etapa ? '  [' + o.etapa + ']' : '')"
                :anchor="{ Width: 380, Height: 36 }"
                :el-style="o.disponible ? moveButtonStyle : disabledButtonStyle"
                @activating="act('evolucionar', o.destino)"
              />
              <Group :anchor="{ Width: 8 }" />
              <Label :text="o.condiciones" :el-style="mutedStyle" :anchor="{ Width: 380, Height: 36 }" />
            </Group>
          </template>
        </template>
        <Label
          v-else
          text="Evolucion no disponible."
          :el-style="mutedStyle"
          :anchor="{ Height: 24 }"
        />

        <Group :anchor="{ Height: 8 }" />
        <Group
          layout-mode="Top"
          :anchor="{ Height: 176 }"
          :background="rowBg"
          :padding="{ Full: 8 }"
        >
          <Label text="CONDICIONES DE EVOLUCION" :el-style="sectionTitleStyle" :anchor="{ Height: 18 }" />
          <Label :text="evolucion.condiciones" :el-style="mutedStyle" :anchor="{ Height: 60 }" />
          <Group :anchor="{ Height: 4 }" />
          <Label :text="evolucion.info" :el-style="goldValueStyle" :anchor="{ Height: 52 }" />
        </Group>
      </template>

      <template v-else-if="pestana === 'de_evolucion'">
        <Group layout-mode="Left" :anchor="{ Height: 22 }">
          <Label text="DE-EVOLUCION" :el-style="sectionTitleStyle" :anchor="{ Width: 360 }" />
          <Label :text="deEvolucion.actual" :el-style="valueStyle" :anchor="{ Width: 128 }" />
        </Group>
        <Group :anchor="{ Height: 2 }" :background="{ Color: '#ff8a5c' }" />
        <Group :anchor="{ Height: 6 }" />
        <Label
          text="La de-evolucion devuelve a tu Digimon a una forma anterior."
          :el-style="mutedStyle"
          :anchor="{ Height: 24 }"
        />

        <template v-if="deEvolucion.opciones.length > 0">
          <TextButton
            v-for="o in deEvolucion.opciones"
            :key="o.destino + (o.disponible ? '-on' : '-off')"
            :text="o.destino"
            :anchor="{ Width: 380, Height: 36 }"
            :el-style="o.disponible ? moveButtonStyle : disabledButtonStyle"
            @activating="act('de_evolucionar', o.destino)"
          />
          <Group :anchor="{ Height: 6 }" />
        </template>
        <Label
          v-else
          text="No se puede de-evolucionar mas."
          :el-style="mutedStyle"
          :anchor="{ Height: 24 }"
        />
        <Group :anchor="{ Height: 6 }" />
        <Group
          layout-mode="Top"
          :anchor="{ Height: 108 }"
          :background="rowBg"
          :padding="{ Full: 8 }"
        >
          <Label text="RESULTADO" :el-style="sectionTitleStyle" :anchor="{ Height: 18 }" />
          <Label :text="deEvolucion.info" :el-style="goldValueStyle" :anchor="{ Height: 56 }" />
        </Group>
      </template>

      <template v-else>
        <Group layout-mode="Left" :anchor="{ Height: 22 }">
          <Label text="EQUIPAMIENTO" :el-style="sectionTitleStyle" :anchor="{ Width: 360 }" />
          <Label text="SLOTS DE DATOS" :el-style="hintStyle" :anchor="{ Width: 128 }" />
        </Group>
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
        <Group :anchor="{ Height: 8 }" />

        <Group
          v-for="fila in [
            { label: 'EQUIPO ACTUAL', key: 'equipo', value: equipamiento.equipo },
            { label: 'ARMA', key: 'arma', value: equipamiento.arma },
            { label: 'ARMADURA', key: 'armadura', value: equipamiento.armadura },
          ]"
          :key="fila.key"
          layout-mode="Left"
          :anchor="{ Height: 44 }"
        >
          <Label :text="fila.label" :el-style="labelStyle" :anchor="{ Width: 140, Height: 42 }" />
          <TextButton
            :text="fila.value"
            :anchor="{ Width: 600, Height: 38 }"
            :el-style="moveButtonStyle"
            @activating="act('equipamiento', fila.key)"
          />
        </Group>

        <Group :anchor="{ Height: 8 }" />
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.magenta }" />
        <Group :anchor="{ Height: 8 }" />
        <Label text="COSMETICOS" :el-style="sectionTitleStyle" :anchor="{ Height: 18 }" />
        <Group layout-mode="Left" :anchor="{ Height: 38 }">
          <TextButton
            :text="equipamiento.cosmetico1"
            :anchor="{ Width: 380, Height: 38 }"
            :el-style="moveButtonStyle"
            @activating="act('equipamiento', 'cosmetico1')"
          />
          <Group :anchor="{ Width: 8 }" />
          <TextButton
            :text="equipamiento.cosmetico2"
            :anchor="{ Width: 380, Height: 38 }"
            :el-style="moveButtonStyle"
            @activating="act('equipamiento', 'cosmetico2')"
          />
        </Group>
      </template>
    </Group>

    <Group :anchor="{ Height: 8 }" />
    <Group layout-mode="Left" :anchor="{ Height: 30 }">
      <Label :text="'ANALIZADOR DIGITAL // ' + titulo" :el-style="captionStyle(CYBER.muted)" :anchor="{ Width: 330, Height: 30 }" />
      <TextButton
        text="CERRAR"
        :anchor="{ Width: 180, Height: 30 }"
        :el-style="buttonStyle(CYBER.danger, 'button_danger')"
        @activating="act('cerrar')"
      />
    </Group>
  </CyberShell>
</template>
