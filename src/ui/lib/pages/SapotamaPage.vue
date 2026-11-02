<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, label, panelBg, rowBg } from '@/cyber'

interface SlotData {
  index: number
  nombre: string
  info: string
  imagen: string | null
  ocupado: boolean
  esA: boolean
  esB: boolean
}

const titulo = useData<string>('titulo', 'DIGIMON COMPANEROS')
const slots = useData<SlotData[]>('slots', [])
const sendAction = useData<(payload: string) => void>('onAction', () => {})
const animarEntrada = useData<boolean>('animarEntrada', true)

const unidades = computed(() => slots.value.filter((s) => s.ocupado).length)

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

function navegar(id: 'equipo' | 'crianza' | 'batalla' | 'perfil') {
  if (id === 'equipo') return
  act('ir_' + id)
}

const sectionTitleStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 11, 'Start', true, { LetterSpacing: 1 }),
)
const slotNameStyle = computed<LabelStyle>(() => label(CYBER.text, 12, 'Start', true))
const slotInfoStyle = computed<LabelStyle>(() => label(CYBER.muted, 10, 'Start'))
const indexStyle = computed<LabelStyle>(() => label(CYBER.gold, 12, 'Center', true))
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))

const smallButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const buttonAStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const buttonBStyle: TextButtonStyle = buttonStyle(CYBER.magenta, 'button_magenta')
const closeButtonStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
</script>

<template>
  <CyberShell
    :animate="animarEntrada"
    title="DIGIMON DATABASE"
    subtitle="SAPOTAMA // EQUIPO"
    active="equipo"
    :status="'UNIDADES ' + unidades + '/6'"
    @nav="navegar"
  >
    <Group
      layout-mode="Top"
      :anchor="{ Height: 420 }"
      :background="panelBg"
      :padding="{ Full: 10 }"
    >
      <Group layout-mode="Left" :anchor="{ Height: 28 }">
        <Label text="UNIDADES ENLAZADAS" :el-style="sectionTitleStyle" :anchor="{ Width: 330 }" />
        <Label text="A / B  ASIGNAR ACTIVO" :el-style="hintStyle" :anchor="{ Width: 158 }" />
      </Group>
      <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
      <Group :anchor="{ Height: 8 }" />

      <Group
        v-for="slot in slots"
        :key="slot.index"
        layout-mode="Left"
        :anchor="{ Height: 56 }"
        :background="rowBg"
        :padding="{ Horizontal: 8, Vertical: 4 }"
      >
        <Label
          :text="'0' + (slot.index + 1)"
          :el-style="indexStyle"
          :anchor="{ Width: 32, Height: 48 }"
        />
        <AssetImage
          :asset-path="slot.imagen || 'UI/Custom/png_Digis/Botamon.png'"
          :visible="slot.ocupado && !!slot.imagen"
          :anchor="{ Width: 42, Height: 42 }"
        />
        <Group :anchor="{ Width: 8 }" />
        <Group layout-mode="Top" :anchor="{ Width: 516, Height: 48 }">
          <Label :text="slot.nombre" :el-style="slotNameStyle" :anchor="{ Height: 26 }" />
          <Label :text="slot.info" :el-style="slotInfoStyle" :anchor="{ Height: 20 }" />
        </Group>
        <TextButton
          text="VER"
          :anchor="{ Width: 80, Height: 38 }"
          :el-style="smallButtonStyle"
          :visible="slot.ocupado"
          @activating="act('ver', slot.index)"
        />
        <Group :anchor="{ Width: 4 }" />
        <TextButton
          text="A"
          :anchor="{ Width: 46, Height: 38 }"
          :el-style="buttonAStyle"
          :visible="slot.ocupado"
          @activating="act('acta', slot.index)"
        />
        <Group :anchor="{ Width: 4 }" />
        <TextButton
          text="B"
          :anchor="{ Width: 46, Height: 38 }"
          :el-style="buttonBStyle"
          :visible="slot.ocupado"
          @activating="act('actb', slot.index)"
        />
      </Group>
    </Group>

    <Group :anchor="{ Height: 8 }" />

    <Group layout-mode="Left" :anchor="{ Height: 38 }">
      <Label
        :text="'DATOS SINCRONIZADOS // ' + titulo"
        :el-style="hintStyle"
        :anchor="{ Width: 330, Height: 38 }"
      />
      <TextButton
        text="CERRAR ENLACE"
        :anchor="{ Width: 180, Height: 38 }"
        :el-style="closeButtonStyle"
        @activating="act('cerrar')"
      />
    </Group>
  </CyberShell>
</template>
