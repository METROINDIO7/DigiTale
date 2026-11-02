<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, label, panelBg } from '@/cyber'

const huevo = useData<string>('huevo', '')
const familia = useData<string>('familia', '')
const ingredientes = useData<string[]>('ingredientes', [])
const entorno = useData<string>('entorno', '')
const energia = useData<string>('energia', '')
const nace = useData<string>('nace', '')
const mensaje = useData<string>('mensaje', '')
const sendAction = useData<(payload: string) => void>('onAction', () => {})

function act(action: string) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action }))
  }
}

const huevoStyle = computed<LabelStyle>(() => label(CYBER.cyan, 15, 'Center', true))
const familyStyle = computed<LabelStyle>(() => label(CYBER.muted, 10, 'Center'))
const sectionStyle = computed<LabelStyle>(() => label(CYBER.gold, 11, 'Center', true, { LetterSpacing: 1 }))
const ingredientStyle = computed<LabelStyle>(() => label(CYBER.text, 10, 'Center'))
const infoStyle = computed<LabelStyle>(() => label(CYBER.text, 10, 'Center', false, { Wrap: true }))
const energyStyle = computed<LabelStyle>(() => label(CYBER.gold, 10, 'Center'))
const resultStyle = computed<LabelStyle>(() => label(CYBER.green, 11, 'Center', true))
const messageStyle = computed<LabelStyle>(() => label(CYBER.gold, 9, 'Center', false, { Wrap: true }))

const unlockButtonStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_gold')
const closeButtonStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
</script>

<template>
  <CyberShell
    title="DATO INCUBACION"
    subtitle="RECETA // BASE DE DATOS"
    :nav="false"
    :animate="false"
    :fondo="false"
    status="DIGITAMA"
  >
    <Group layout-mode="Middle" :anchor="{ Height: 470 }">
      <Group
        layout-mode="Top"
        :anchor="{ Width: 720, Height: 450 }"
        :background="panelBg"
        :padding="{ Full: 14 }"
      >
        <Label :text="huevo" :el-style="huevoStyle" :anchor="{ Height: 24 }" />
        <Label :text="familia" :el-style="familyStyle" :anchor="{ Height: 18 }" />
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.gold }" />
        <Group :anchor="{ Height: 8 }" />

        <Label text="INGREDIENTES" :el-style="sectionStyle" :anchor="{ Height: 20 }" />
        <Label
          v-for="(ing, i) in ingredientes"
          :key="i"
          :text="'- ' + ing"
          :el-style="ingredientStyle"
          :anchor="{ Height: 18 }"
        />
        <Group :anchor="{ Height: 8 }" />

        <Label :text="entorno" :el-style="infoStyle" :anchor="{ Height: 32 }" />
        <Label :text="energia" :el-style="energyStyle" :anchor="{ Height: 18 }" />
        <Label :text="nace" :el-style="resultStyle" :anchor="{ Height: 22 }" />
        <Group :anchor="{ Height: 6 }" />
        <Label :text="mensaje" :el-style="messageStyle" :anchor="{ Height: 20 }" />
        <Group :anchor="{ Height: 6 }" />

        <Group layout-mode="Middle" :anchor="{ Height: 40 }">
          <Group layout-mode="Left" :anchor="{ Width: 412, Height: 38 }">
            <TextButton
              text="DESBLOQUEAR"
              :anchor="{ Width: 240, Height: 38 }"
              :el-style="unlockButtonStyle"
              @activating="act('desbloquear')"
            />
            <Group :anchor="{ Width: 12 }" />
            <TextButton
              text="CERRAR"
              :anchor="{ Width: 160, Height: 38 }"
              :el-style="closeButtonStyle"
              @activating="act('cerrar')"
            />
          </Group>
        </Group>
      </Group>
    </Group>
  </CyberShell>
</template>
