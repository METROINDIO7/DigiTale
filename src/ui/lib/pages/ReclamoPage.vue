<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vt:@core/composables/useData'
import type { LabelStyle, TextButtonStyle } from 'vuetale/types/elements'
import CyberShell from '@/components/CyberShell.vue'
import { CYBER, buttonStyle, label, panelBg, rowBg } from '@/cyber'

interface BankItem {
  indice: number
  nombre: string
  info: string
  imagen: string
  ocupado: boolean
}

const tab = useData<number>('tab', 0)
const tieneToken = useData<boolean>('tieneToken', false)
const tokenTexto = useData<string>('tokenTexto', '')
const imagen = useData<string>('imagen', '')
const banco = useData<BankItem[]>('banco', [])
const conteo = useData<string>('conteo', '')
const mensaje = useData<string>('mensaje', '')
const sendAction = useData<(payload: string) => void>('onAction', () => {})

function act(action: string, value?: string | number) {
  const fn = sendAction.value
  if (typeof fn === 'function') {
    fn(JSON.stringify({ action, value }))
  }
}

const tokenStyle = computed<LabelStyle>(() => label(CYBER.cyan, 11, 'Center', true, { Wrap: true }))
const hintStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Center'))
const sectionStyle = computed<LabelStyle>(() => label(CYBER.gold, 10, 'Start', true, { LetterSpacing: 1 }))
const itemNameStyle = computed<LabelStyle>(() => label(CYBER.text, 10, 'Start', true))
const itemInfoStyle = computed<LabelStyle>(() => label(CYBER.muted, 9, 'Start'))
const messageStyle = computed<LabelStyle>(() => label(CYBER.gold, 9, 'Start', false, { Wrap: true }))

const tabActiveStyle: TextButtonStyle = buttonStyle(CYBER.gold, 'button_gold')
const tabIdleStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const teamButtonStyle: TextButtonStyle = buttonStyle(CYBER.green)
const bankButtonStyle: TextButtonStyle = buttonStyle(CYBER.cyan)
const releaseButtonStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
const closeButtonStyle: TextButtonStyle = buttonStyle(CYBER.danger, 'button_danger')
</script>

<template>
  <CyberShell
    title="RECLAMO DE CRIAS"
    subtitle="INCUBACION // MESA"
    :nav="false"
    :animate="false"
    :fondo="false"
    status="INCUBADORA"
  >
    <Group layout-mode="Middle" :anchor="{ Height: 470 }">
      <Group
        layout-mode="Top"
        :anchor="{ Width: 760, Height: 460 }"
        :background="panelBg"
        :padding="{ Full: 12 }"
      >
        <!-- Pestañas -->
        <Group layout-mode="Left" :anchor="{ Height: 36 }">
          <TextButton
            :key="'tab-rec-' + (tab === 0)"
            text="RECLAMAR"
            :anchor="{ Width: 362, Height: 34 }"
            :el-style="tab === 0 ? tabActiveStyle : tabIdleStyle"
            @activating="act('tablistas')"
          />
          <Group :anchor="{ Width: 8 }" />
          <TextButton
            :key="'tab-bco-' + (tab === 1)"
            text="BANCO PC"
            :anchor="{ Width: 362, Height: 34 }"
            :el-style="tab === 1 ? tabActiveStyle : tabIdleStyle"
            @activating="act('tabbanco')"
          />
        </Group>
        <Group :anchor="{ Height: 6 }" />

        <!-- Reclamar -->
        <Group layout-mode="Top" :anchor="{ Height: 320 }" :visible="tab === 0">
          <Label :text="tokenTexto" :el-style="tokenStyle" :anchor="{ Height: 50 }" />
          <Group layout-mode="Middle" :anchor="{ Height: 70 }">
            <AssetImage
              :asset-path="imagen || 'UI/Custom/png_Digis/Botamon.png'"
              :visible="tieneToken && !!imagen"
              :anchor="{ Width: 64, Height: 64 }"
            />
          </Group>
          <Group layout-mode="Middle" :anchor="{ Height: 44 }">
            <Group layout-mode="Left" :anchor="{ Width: 466, Height: 40 }">
              <TextButton
                text="EQUIPO"
                :anchor="{ Width: 150, Height: 40 }"
                :el-style="teamButtonStyle"
                :visible="tieneToken"
                @activating="act('equipo')"
              />
              <Group :anchor="{ Width: 8 }" />
              <TextButton
                text="BANCO"
                :anchor="{ Width: 150, Height: 40 }"
                :el-style="bankButtonStyle"
                :visible="tieneToken"
                @activating="act('banco')"
              />
              <Group :anchor="{ Width: 8 }" />
              <TextButton
                text="LIBERAR"
                :anchor="{ Width: 150, Height: 40 }"
                :el-style="releaseButtonStyle"
                :visible="tieneToken"
                @activating="act('liberar')"
              />
            </Group>
          </Group>
          <Label
            text="EQUIPO = al equipo  |  BANCO = a la caja  |  LIBERAR = descartar"
            :el-style="hintStyle"
            :anchor="{ Height: 20 }"
          />
        </Group>

        <!-- Banco PC -->
        <Group layout-mode="Top" :anchor="{ Height: 320 }" :visible="tab === 1">
          <Group layout-mode="Left" :anchor="{ Height: 22 }">
            <Label text="BANCO PC // RETIRAR" :el-style="sectionStyle" :anchor="{ Width: 500 }" />
            <Label :text="conteo" :el-style="itemInfoStyle" :anchor="{ Width: 224 }" />
          </Group>
          <Group :anchor="{ Height: 4 }" />
          <Group
            v-for="item in banco"
            :key="item.indice"
            layout-mode="Left"
            :anchor="{ Height: 34 }"
            :background="rowBg"
            :padding="{ Horizontal: 6, Vertical: 2 }"
          >
            <AssetImage
              :asset-path="item.imagen || 'UI/Custom/png_Digis/Botamon.png'"
              :visible="item.ocupado && !!item.imagen"
              :anchor="{ Width: 28, Height: 28 }"
            />
            <Group :anchor="{ Width: 6 }" />
            <Group layout-mode="Top" :anchor="{ Width: 340, Height: 30 }">
              <Label :text="item.nombre" :el-style="itemNameStyle" :anchor="{ Height: 17 }" />
              <Label :text="item.info" :el-style="itemInfoStyle" :anchor="{ Height: 13 }" />
            </Group>
            <TextButton
              text="EQUIPO"
              :anchor="{ Width: 82, Height: 30 }"
              :el-style="teamButtonStyle"
              :visible="item.ocupado"
              @activating="act('bequipo', item.indice)"
            />
            <Group :anchor="{ Width: 6 }" />
            <TextButton
              text="LIBERAR"
              :anchor="{ Width: 78, Height: 30 }"
              :el-style="releaseButtonStyle"
              :visible="item.ocupado"
              @activating="act('bliberar', item.indice)"
            />
          </Group>
        </Group>

        <Group :anchor="{ Height: 6 }" />
        <Group layout-mode="Left" :anchor="{ Height: 40 }">
          <Label :text="mensaje" :el-style="messageStyle" :anchor="{ Width: 520, Height: 40 }" />
          <Group :anchor="{ Width: 6 }" />
          <TextButton
            text="CERRAR"
            :anchor="{ Width: 200, Height: 36 }"
            :el-style="closeButtonStyle"
            @activating="act('cerrar')"
          />
        </Group>
      </Group>
    </Group>
  </CyberShell>
</template>
