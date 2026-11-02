<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, label, rowBg } from '@/cyber'

interface BatallaData {
  desafioAbierto: boolean
  modo: string
  activos: string
  info: string
  retoDe: string
}

const batalla = useData<BatallaData>('batalla', {
  desafioAbierto: false,
  modo: 'Amistoso',
  activos: '',
  info: '',
  retoDe: '',
})
const sendAction = useData<(payload: string) => void>('onAction', () => {})
const animarEntrada = useData<boolean>('animarEntrada', true)

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

function navegar(id: 'equipo' | 'crianza' | 'batalla' | 'perfil') {
  if (id === 'batalla') return
  act('ir_' + id)
}

const titleLabelStyle = computed<LabelStyle>(() =>
  label(CYBER.cyan, 13, 'Start', true, { LetterSpacing: 1 }),
)
const sectionTitleStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 11, 'Start', true, { LetterSpacing: 1 }),
)
const mutedStyle = computed<LabelStyle>(() => label(CYBER.muted, 10, 'Start', false, { Wrap: true }))
const valueStyle = computed<LabelStyle>(() => label(CYBER.text, 11, 'Start', false, { Wrap: true }))
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))

const primaryButtonStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_gold')
const smallButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const closeButtonStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
</script>

<template>
  <CyberShell
    :animate="animarEntrada"
    title="DIGIMON BATTLE"
    subtitle="BATALLA // PVP"
    active="batalla"
    :status="'MODO ' + batalla.modo"
    @nav="navegar"
  >
    <Group layout-mode="Left" :anchor="{ Height: 28 }">
      <Label text="BATALLA PVP" :el-style="sectionTitleStyle" :anchor="{ Width: 330 }" />
      <Label
        :text="batalla.desafioAbierto ? 'ENLACE ABIERTO' : 'ENLACE CERRADO'"
        :el-style="hintStyle"
        :anchor="{ Width: 158 }"
      />
    </Group>
    <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.magenta }" />
    <Group :anchor="{ Height: 10 }" />

    <Group
      layout-mode="Top"
      :anchor="{ Height: 180 }"
      :background="rowBg"
      :padding="{ Full: 12 }"
    >
      <Label
        :text="batalla.retoDe ? 'RETO DE ' + batalla.retoDe : 'SIN RETOS PENDIENTES'"
        :el-style="titleLabelStyle"
        :anchor="{ Height: 28 }"
      />
      <Label :text="'MODO: ' + batalla.modo" :el-style="mutedStyle" :anchor="{ Height: 26 }" />
      <Label :text="batalla.activos" :el-style="valueStyle" :anchor="{ Height: 26 }" />
      <Label :text="batalla.info" :el-style="mutedStyle" :anchor="{ Height: 64 }" />
    </Group>

    <Group :anchor="{ Height: 10 }" />

    <Group layout-mode="Left" :anchor="{ Height: 48 }">
      <TextButton
        :text="batalla.desafioAbierto ? 'CERRAR DESAFIO' : 'ABRIR DESAFIO'"
        :anchor="{ Width: 320, Height: 44 }"
        :el-style="primaryButtonStyle"
        @activating="act('desafio')"
      />
      <Group :anchor="{ Width: 10 }" />
      <TextButton
        :text="'MODO: ' + batalla.modo"
        :anchor="{ Width: 300, Height: 44 }"
        :el-style="smallButtonStyle"
        @activating="act('modo')"
      />
    </Group>

    <Group :anchor="{ Height: 10 }" />

    <Group
      layout-mode="Top"
      :anchor="{ Height: 110 }"
      :background="rowBg"
      :padding="{ Full: 12 }"
    >
      <Label text="COMO RETAR" :el-style="sectionTitleStyle" :anchor="{ Height: 22 }" />
      <Label
        text="Golpea con el sapotama a un rival con desafio abierto. Los Digimon salvajes inician combate al ser golpeados."
        :el-style="mutedStyle"
        :anchor="{ Height: 48 }"
      />
    </Group>

    <Group :anchor="{ Height: 10 }" />

    <Group layout-mode="Left" :anchor="{ Height: 38 }">
      <Label
        text="SISTEMA DE COMBATE // PVP"
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
