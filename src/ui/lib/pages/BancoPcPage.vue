<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, label, panelBg, rowBg } from '@/cyber'

interface PcItem {
  indice: number
  nombre: string
  info: string
  imagen: string
  ocupado: boolean
}

const equipo = useData<PcItem[]>('equipo', [])
const banco = useData<PcItem[]>('banco', [])
const conteo = useData<string>('conteo', '')
const mensaje = useData<string>('mensaje', '')
const sendAction = useData<(payload: string) => void>('onAction', () => {})
const animarEntrada = useData<boolean>('animarEntrada', true)

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

const sectionTitleStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 11, 'Start', true, { LetterSpacing: 1 }),
)
const countStyle = computed<LabelStyle>(() => label(CYBER.cyan, 11, 'End', true))
const itemNameStyle = computed<LabelStyle>(() => label(CYBER.text, 11, 'Start', true))
const itemInfoStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start'))
const messageStyle = computed<LabelStyle>(() =>
  label(CYBER.gold, 10, 'Start', false, { Wrap: true }),
)
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))

const saveButtonStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_gold')
const takeButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const closeButtonStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
</script>

<template>
  <CyberShell
    :animate="animarEntrada"
    title="PC DIGITAL"
    subtitle="CAJA DE DATOS // TRANSFERENCIA"
    :nav="false"
    status="SISTEMA PC"
  >
    <Group layout-mode="Left" :anchor="{ Height: 26 }">
      <Label text="GESTION DE DATOS" :el-style="sectionTitleStyle" :anchor="{ Width: 400 }" />
      <Label :text="conteo" :el-style="countStyle" :anchor="{ Width: 560 }" />
    </Group>
    <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
    <Group :anchor="{ Height: 8 }" />

    <Group layout-mode="Left" :anchor="{ Height: 360 }">
      <!-- EQUIPO: depositar en la caja -->
      <Group
        layout-mode="Top"
        :anchor="{ Width: 478, Height: 360 }"
        :background="panelBg"
        :padding="{ Full: 8 }"
      >
        <Label text="EQUIPO // GUARDAR" :el-style="sectionTitleStyle" :anchor="{ Height: 22 }" />
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.gold }" />
        <Group :anchor="{ Height: 6 }" />
        <Group
          v-for="item in equipo"
          :key="item.indice"
          layout-mode="Left"
          :anchor="{ Height: 44 }"
          :background="rowBg"
          :padding="{ Horizontal: 6, Vertical: 3 }"
        >
          <AssetImage
            :asset-path="item.imagen || 'UI/Custom/png_Digis/Botamon.png'"
            :visible="item.ocupado && !!item.imagen"
            :anchor="{ Width: 34, Height: 34 }"
          />
          <Group :anchor="{ Width: 6 }" />
          <Group layout-mode="Top" :anchor="{ Width: 326, Height: 38 }">
            <Label :text="item.nombre" :el-style="itemNameStyle" :anchor="{ Height: 20 }" />
            <Label :text="item.info" :el-style="itemInfoStyle" :anchor="{ Height: 16 }" />
          </Group>
          <TextButton
            text="GUARDAR"
            :anchor="{ Width: 90, Height: 34 }"
            :el-style="saveButtonStyle"
            :visible="item.ocupado"
            @activating="act('depositar', item.indice)"
          />
        </Group>
      </Group>

      <Group :anchor="{ Width: 8 }" />

      <!-- CAJA: retirar al equipo -->
      <Group
        layout-mode="Top"
        :anchor="{ Width: 486, Height: 360 }"
        :background="panelBg"
        :padding="{ Full: 8 }"
      >
        <Label text="CAJA // RETIRAR" :el-style="sectionTitleStyle" :anchor="{ Height: 22 }" />
        <Group :anchor="{ Height: 2 }" :background="{ Color: CYBER.cyan }" />
        <Group :anchor="{ Height: 6 }" />
        <Group
          v-for="item in banco"
          :key="item.indice"
          layout-mode="Left"
          :anchor="{ Height: 38 }"
          :background="rowBg"
          :padding="{ Horizontal: 6, Vertical: 2 }"
        >
          <AssetImage
            :asset-path="item.imagen || 'UI/Custom/png_Digis/Botamon.png'"
            :visible="item.ocupado && !!item.imagen"
            :anchor="{ Width: 30, Height: 30 }"
          />
          <Group :anchor="{ Width: 6 }" />
          <Group layout-mode="Top" :anchor="{ Width: 336, Height: 32 }">
            <Label :text="item.nombre" :el-style="itemNameStyle" :anchor="{ Height: 18 }" />
            <Label :text="item.info" :el-style="itemInfoStyle" :anchor="{ Height: 14 }" />
          </Group>
          <TextButton
            text="RETIRAR"
            :anchor="{ Width: 88, Height: 30 }"
            :el-style="takeButtonStyle"
            :visible="item.ocupado"
            @activating="act('retirar', item.indice)"
          />
        </Group>
      </Group>
    </Group>

    <Group :anchor="{ Height: 6 }" />
    <Group layout-mode="Left" :anchor="{ Height: 40 }">
      <Label :text="mensaje" :el-style="messageStyle" :anchor="{ Width: 700, Height: 40 }" />
      <Group :anchor="{ Width: 10 }" />
      <TextButton
        text="CERRAR"
        :anchor="{ Width: 200, Height: 36 }"
        :el-style="closeButtonStyle"
        @activating="act('cerrar')"
      />
    </Group>
  </CyberShell>
</template>
