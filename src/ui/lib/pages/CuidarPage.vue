<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, captionStyle, label, panelBg, rowBg } from '@/cyber'

interface CuidarSlot {
  index: number
  nombre: string
  info: string
  imagen?: string | null
  ocupado: boolean
  dormido: boolean
  paseando: boolean
  esActivo: boolean
}

interface ComidaData {
  index: number
  nombre: string
  cantidad: number
  h: number
  e: number
  hpPct: number
}

const titulo = useData<string>('titulo', 'CRIANZA DE DIGIMON')
const slots = useData<CuidarSlot[]>('slots', [])
const comidas = useData<ComidaData[]>('comidas', [])
const slotComida = useData<number>('slotComida', -1)
const comidaPara = useData<string>('comidaPara', '')
const info = useData<string>('info', '-')
const sendAction = useData<(payload: string) => void>('onAction', () => {})
const animarEntrada = useData<boolean>('animarEntrada', true)

const filas = computed<CuidarSlot[][]>(() => {
  const out: CuidarSlot[][] = []
  const data = slots.value
  for (let i = 0; i < data.length; i += 2) {
    out.push(data.slice(i, i + 2))
  }
  return out
})

const enSeleccionComida = computed(() => slotComida.value >= 0)
const unidades = computed(() => slots.value.filter((s) => s.ocupado).length)

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

function navegar(id: 'equipo' | 'crianza' | 'batalla' | 'perfil') {
  if (id === 'equipo') {
    act('volver')
  } else if (id === 'perfil') {
    act('ir_perfil')
  } else if (id === 'batalla') {
    act('ir_batalla')
  }
}

const sectionTitleStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 11, 'Start', true, { LetterSpacing: 1 }),
)
const mutedStyle = computed<LabelStyle>(() => label(CYBER.muted, 10, 'Start', false, { Wrap: true }))
const slotNameStyle = computed<LabelStyle>(() => label(CYBER.text, 11, 'Start', true))
const slotInfoStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start'))
const infoStyle = computed<LabelStyle>(() => label(CYBER.text, 10, 'Start', false, { Wrap: true }))
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))
const indexStyle = computed<LabelStyle>(() => label(CYBER.gold, 10, 'Center', true))

const smallButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const goldButtonStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_gold')
const blueButtonStyle: TextButtonStyle = buttonStyle(CYBER.magenta, 'button_magenta')
const backButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
</script>

<template>
  <CyberShell
    :animate="animarEntrada"
    title="DIGIMON CARE"
    subtitle="CRIANZA // CONTROL DE ESTADO"
    active="crianza"
    :status="'UNIDADES ' + unidades + '/6'"
    @nav="navegar"
  >
    <Group layout-mode="Left" :anchor="{ Height: 28 }">
      <Label
        :text="enSeleccionComida ? 'SELECCION DE ALIMENTO' : 'CUIDA A TU EQUIPO'"
        :el-style="sectionTitleStyle"
        :anchor="{ Width: 340 }"
      />
      <Label text="H / E / L" :el-style="hintStyle" :anchor="{ Width: 158 }" />
    </Group>
    <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
    <Group :anchor="{ Height: 6 }" />

    <template v-if="enSeleccionComida">
      <Label :text="comidaPara" :el-style="mutedStyle" :anchor="{ Height: 22 }" />
      <Group
        v-for="c in comidas"
        :key="c.index"
        layout-mode="Left"
        :anchor="{ Height: 46 }"
        :background="rowBg"
        :padding="{ Horizontal: 8, Vertical: 4 }"
      >
        <Label
          :text="c.nombre + ' x' + c.cantidad + '   H+' + c.h + '  E+' + c.e"
          :el-style="slotNameStyle"
          :anchor="{ Width: 520, Height: 36 }"
        />
        <TextButton
          text="DAR"
          :anchor="{ Width: 120, Height: 36 }"
          :el-style="goldButtonStyle"
          @activating="act('dar_comida', c.index)"
        />
      </Group>

      <Group :anchor="{ Height: 10 }" />
      <TextButton
        text="VOLVER"
        :anchor="{ Width: 180, Height: 38 }"
        :el-style="backButtonStyle"
        @activating="act('comida_volver')"
      />
    </template>

    <template v-else>
      <Group
        v-for="(fila, fi) in filas"
        :key="fi"
        layout-mode="Left"
        :anchor="{ Height: 102 }"
      >
        <Group
          v-for="slot in fila"
          :key="slot.index"
          layout-mode="Top"
          :anchor="{ Width: 400, Height: 100 }"
          :background="panelBg"
          :padding="{ Full: 7 }"
        >
          <Group layout-mode="Left" :anchor="{ Height: 46 }">
            <Label
              :text="'0' + (slot.index + 1)"
              :el-style="indexStyle"
              :anchor="{ Width: 24, Height: 44 }"
            />
            <AssetImage
              :asset-path="slot.imagen || 'UI/Custom/png_Digis/Botamon.png'"
              :visible="slot.ocupado && !!slot.imagen"
              :anchor="{ Width: 44, Height: 44 }"
            />
            <Group :anchor="{ Width: 6 }" />
            <Group layout-mode="Top" :anchor="{ Width: 306, Height: 46 }">
              <Label
                :text="slot.ocupado ? slot.nombre : 'SIN DATOS'"
                :el-style="slotNameStyle"
                :anchor="{ Height: 22 }"
              />
              <Label :text="slot.info" :el-style="slotInfoStyle" :anchor="{ Height: 20 }" />
            </Group>
          </Group>
          <Group :anchor="{ Height: 4 }" />
          <Group layout-mode="Left" :anchor="{ Height: 26 }">
            <TextButton
              text="COMER"
              :anchor="{ Width: 72, Height: 30 }"
              :el-style="smallButtonStyle"
              :visible="slot.ocupado && !slot.dormido"
              @activating="act('alimentar', slot.index)"
            />
            <Group :anchor="{ Width: 4 }" />
            <TextButton
              :text="slot.dormido ? 'DESP.' : 'DORMIR'"
              :anchor="{ Width: 72, Height: 30 }"
              :el-style="smallButtonStyle"
              :visible="slot.ocupado"
              @activating="act('dormir', slot.index)"
            />
            <Group :anchor="{ Width: 4 }" />
            <TextButton
              text="PASEO"
              :anchor="{ Width: 68, Height: 30 }"
              :el-style="smallButtonStyle"
              :visible="slot.ocupado"
              @activating="act('jugar', slot.index)"
            />
            <Group :anchor="{ Width: 4 }" />
            <TextButton
              text="BANCO"
              :anchor="{ Width: 68, Height: 30 }"
              :el-style="blueButtonStyle"
              :visible="slot.ocupado"
              @activating="act('banco', slot.index)"
            />
          </Group>
        </Group>
        <Group :anchor="{ Width: 8 }" />
      </Group>

      <Group :anchor="{ Height: 6 }" />
      <Group
        layout-mode="Top"
        :anchor="{ Height: 80 }"
        :background="rowBg"
        :padding="{ Full: 8 }"
      >
        <Label text="ULTIMA ACCION" :el-style="sectionTitleStyle" :anchor="{ Height: 20 }" />
        <Label :text="info" :el-style="infoStyle" :anchor="{ Height: 44 }" />
      </Group>

      <Group layout-mode="Left" :anchor="{ Height: 38 }">
        <Label
          :text="'SISTEMA DE CRIANZA // ' + titulo"
          :el-style="captionStyle(CYBER.muted)"
          :anchor="{ Width: 330, Height: 38 }"
        />
        <TextButton
          text="VOLVER AL EQUIPO"
          :anchor="{ Width: 180, Height: 38 }"
          :el-style="backButtonStyle"
          @activating="act('volver')"
        />
      </Group>
    </template>
  </CyberShell>
</template>
