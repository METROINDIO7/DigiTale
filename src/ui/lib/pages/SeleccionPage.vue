<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, captionStyle, label, panelBg, panelSoftBg } from '@/cyber'

interface StarterData {
  id: string
  nombre: string
  imagen: string
  elemento: string
  descripcion: string
}

const starters = useData<StarterData[]>('starters', [])
const seleccionada = useData<string>('seleccionada', '')
const mensaje = useData<string>('mensaje', 'SISTEMA ESPERANDO SELECCION')
const sendAction = useData<(payload: string) => void>('onAction', () => {})
const animarEntrada = useData<boolean>('animarEntrada', true)

const elegido = computed(() => starters.value.find((s) => s.id === seleccionada.value))

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

const sectionTitleStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 11, 'Start', true, { LetterSpacing: 1 }),
)
const nameStyle = computed<LabelStyle>(() => label(CYBER.text, 11, 'Start', true))
const mutedStyle = computed<LabelStyle>(() => label(CYBER.muted, 10, 'Start', false, { Wrap: true }))
const bigNameStyle = computed<LabelStyle>(() => label(CYBER.text, 18, 'Start', true, { LetterSpacing: 1 }))
const statusStyle = computed<LabelStyle>(() => label(CYBER.cyan, 10, 'Start', false, { LetterSpacing: 1 }))
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))

const pickStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const confirmStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_gold')
const cancelStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
</script>

<template>
  <CyberShell
    :animate="animarEntrada"
    title="DIGI-EGG SYSTEM"
    subtitle="INICIALIZACION // ELIGE TU COMPANERO"
    :nav="false"
    status="PRIMER ENLACE"
  >
    <Group layout-mode="Left" :anchor="{ Height: 32 }">
      <Label text="UNIDADES DISPONIBLES" :el-style="sectionTitleStyle" :anchor="{ Width: 400 }" />
      <Label :text="mensaje" :el-style="statusStyle" :anchor="{ Width: 258, Height: 24 }" />
    </Group>
    <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
    <Group :anchor="{ Height: 6 }" />

    <Group
      v-for="fila in [0, 1]"
      :key="fila"
      layout-mode="Left"
      :anchor="{ Height: 120 }"
    >
      <Group
        v-for="(s, idx) in starters.slice(fila * 4, fila * 4 + 4)"
        :key="s.id"
        layout-mode="Left"
        :anchor="{ Height: 120 }"
      >
        <Group
          :key="s.id + (seleccionada === s.id ? '-sel' : '-idle')"
          layout-mode="Top"
          :anchor="{ Width: 230, Height: 104 }"
          :background="seleccionada === s.id ? panelBg : panelSoftBg"
          :padding="{ Full: 6 }"
        >
          <Group layout-mode="Left" :anchor="{ Height: 56 }">
            <AssetImage :asset-path="s.imagen" :anchor="{ Width: 56, Height: 56 }" />
            <Group :anchor="{ Width: 6 }" />
            <Group layout-mode="Top" :anchor="{ Width: 156, Height: 56 }">
              <Label :text="s.nombre" :el-style="nameStyle" :anchor="{ Height: 20 }" />
              <Label :text="s.elemento" :el-style="captionStyle(CYBER.cyan)" :anchor="{ Height: 18 }" />
            </Group>
          </Group>
          <Group :anchor="{ Height: 4 }" />
          <TextButton
            :text="seleccionada === s.id ? 'SELECCIONADO' : 'ELEGIR'"
            :anchor="{ Height: 32 }"
            :el-style="pickStyle"
            @activating="act('seleccionar', s.id)"
          />
        </Group>
        <Group v-if="idx < 3" :anchor="{ Width: 8 }" />
      </Group>
    </Group>

    <Group :anchor="{ Height: 6 }" />
    <Group
      layout-mode="Left"
      :anchor="{ Height: 120 }"
      :background="panelBg"
      :padding="{ Full: 8 }"
    >
      <AssetImage
        :asset-path="elegido?.imagen || 'UI/Custom/png_Digis/Botamon.png'"
        :visible="!!elegido"
        :anchor="{ Width: 96, Height: 96 }"
      />
      <Group :anchor="{ Width: 10 }" />
      <Group layout-mode="Top" :anchor="{ Width: 840, Height: 96 }">
        <Label :text="elegido?.nombre || 'SIN SELECCION'" :el-style="bigNameStyle" :anchor="{ Height: 32 }" />
        <Label
          :text="elegido ? 'ELEMENTO // ' + elegido.elemento : 'Elige una unidad para iniciar el enlace.'"
          :el-style="statusStyle"
          :anchor="{ Height: 18 }"
        />
        <Group :anchor="{ Height: 4 }" />
        <Label
          :text="elegido?.descripcion || 'Los datos del Digimon apareceran aqui.'"
          :el-style="mutedStyle"
          :anchor="{ Height: 42 }"
        />
      </Group>
    </Group>

    <Group :anchor="{ Height: 8 }" />
    <Group layout-mode="Left" :anchor="{ Height: 42 }">
      <TextButton
        text="CONFIRMAR ENLACE"
        :anchor="{ Width: 220, Height: 34 }"
        :el-style="confirmStyle"
        @activating="act('confirmar')"
      />
      <Group :anchor="{ Width: 8 }" />
      <TextButton
        text="CANCELAR"
        :anchor="{ Width: 160, Height: 34 }"
        :el-style="cancelStyle"
        @activating="act('cancelar')"
      />
      <Group :anchor="{ Width: 8 }" />
      <Label text="8 UNIDADES // 1 ELECCION" :el-style="hintStyle" :anchor="{ Width: 260, Height: 34 }" />
    </Group>
  </CyberShell>
</template>
