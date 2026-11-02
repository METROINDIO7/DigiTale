<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  CYBER,
  captionStyle,
  headerBg,
  label,
  navActiveStyle,
  navStyle,
  panelSoftBg,
  titleStyle,
} from '@/cyber'

type NavId = 'equipo' | 'crianza' | 'batalla' | 'perfil'

const props = withDefaults(
  defineProps<{
    title?: string
    subtitle?: string
    active?: NavId
    nav?: boolean
    status?: string
    animate?: boolean
    fondo?: boolean
  }>(),
  {
    title: 'DIGITALE',
    subtitle: 'DIGITAL NETWORK',
    active: 'equipo',
    nav: true,
    status: 'LINK ESTABLE',
    animate: true,
    fondo: true,
  },
)

const emit = defineEmits<{ (event: 'nav', id: NavId): void }>()

const items: { id: NavId; label: string; code: string }[] = [
  { id: 'equipo', label: 'EQUIPO', code: '01' },
  { id: 'crianza', label: 'CRIANZA', code: '02' },
  { id: 'batalla', label: 'BATALLA', code: '03' },
  { id: 'perfil', label: 'PERFIL', code: '04' },
]

const contentWidth = computed(() => (props.nav ? 816 : 972))

// ── Animacion CRT (solo encendido) ─────────────────────────────────
const overlayFase = ref<'negro' | 'fade1' | 'fade2' | 'fade3' | 'fade4' | 'none'>(
  props.animate ? 'negro' : 'none',
)
const lineaFase = ref<0 | 1 | 2 | 3>(0)
let timers: number[] = []

function limpiarTimers() {
  for (const t of timers) clearTimeout(t)
  timers = []
}

function programar(ms: number, fn: () => void) {
  timers.push(setTimeout(fn, ms))
}

function encender() {
  limpiarTimers()
  overlayFase.value = 'negro'
  lineaFase.value = 0
  // Linea horizontal que se expande (TV viejo).
  programar(80, () => (lineaFase.value = 1))
  programar(160, () => (lineaFase.value = 2))
  programar(240, () => (lineaFase.value = 3))
  // Fundido de negro a la interfaz.
  programar(300, () => { lineaFase.value = 0; overlayFase.value = 'fade1' })
  programar(360, () => (overlayFase.value = 'fade2'))
  programar(420, () => (overlayFase.value = 'fade3'))
  programar(480, () => (overlayFase.value = 'fade4'))
  programar(540, () => (overlayFase.value = 'none'))
}

onMounted(() => { if (props.animate) encender() })
onBeforeUnmount(limpiarTimers)

const subtitleStyle = computed(() => captionStyle(CYBER.cyan))
const headerTitleStyle = computed(() => titleStyle(CYBER.cyan))
const headerSubtitleStyle = computed(() =>
  label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 2, RenderUppercase: true }),
)
const netStyle = computed(() => label(CYBER.gold, 11, 'End', true, { LetterSpacing: 1 }))
const navTitleStyle = computed(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 2 }))
const footerStyle = computed(() => label(CYBER.muted, 9, 'Start', false, { LetterSpacing: 1 }))
const hintStyle = computed(() => label(CYBER.muted, 9, 'End', false, { LetterSpacing: 1 }))
</script>

<template>
  <AssetImage v-if="fondo" :asset-path="'UI/FondoUI.png'" />
  <Group
    layout-mode="Top"
    :anchor="{ Width: 1020, Height: 620 }"
    :background="fondo ? { Color: '#04070dcc' } : { Color: '#04070d' }"
    :padding="{ Full: 12 }"
  >
    <Group
      layout-mode="Left"
      :anchor="{ Height: 68 }"
      :background="headerBg"
      :padding="{ Full: 8 }"
    >
      <Group layout-mode="Top" :anchor="{ Width: 700, Height: 52 }">
        <Label :text="title" :el-style="headerTitleStyle" :anchor="{ Height: 32 }" />
        <Label :text="subtitle" :el-style="headerSubtitleStyle" :anchor="{ Height: 20 }" />
      </Group>
      <Group layout-mode="Top" :anchor="{ Width: 240, Height: 52 }">
        <Label text="/// CYBER SYSTEM" :el-style="subtitleStyle" :anchor="{ Height: 24 }" />
        <Label :text="status" :el-style="netStyle" :anchor="{ Height: 24 }" />
      </Group>
    </Group>

    <Group :anchor="{ Height: 6 }" />

    <Group layout-mode="Left" :anchor="{ Height: 480 }">
      <Group
        v-if="nav"
        layout-mode="Top"
        :anchor="{ Width: 170, Height: 480 }"
        :background="panelSoftBg"
        :padding="{ Full: 8 }"
      >
        <Label text="SECCIONES" :el-style="navTitleStyle" :anchor="{ Height: 20 }" />
        <Group :anchor="{ Height: 4 }" />
        <!-- El :key cambia con el estado activo: si solo cambiara el :el-style,
             Vuetale actualizaria el Background de un boton ya renderizado y el
             cliente de Hytale se congela (limitacion conocida de Vuetale). -->
        <TextButton
          v-for="item in items"
          :key="item.id + (active === item.id ? '-on' : '-off')"
          :text="(active === item.id ? '> ' : '  ') + item.code + '  ' + item.label"
          :anchor="{ Height: 40 }"
          :el-style="active === item.id ? navActiveStyle : navStyle"
          @activating="emit('nav', item.id)"
        />
        <Group :anchor="{ Height: 10 }" />
        <Label text="DIGIMON STORY" :el-style="captionStyle(CYBER.muted)" :anchor="{ Height: 16 }" />
        <Label text="CYBER SLEUTH" :el-style="captionStyle(CYBER.cyan)" :anchor="{ Height: 16 }" />
        <Label text="INSPIRED UI" :el-style="captionStyle(CYBER.muted)" :anchor="{ Height: 16 }" />
      </Group>

      <Group v-if="nav" :anchor="{ Width: 10 }" />

      <Group layout-mode="Top" :anchor="{ Width: contentWidth, Height: 480 }">
        <slot />
      </Group>
    </Group>

    <Group :anchor="{ Height: 8 }" />

    <Group layout-mode="Left" :anchor="{ Height: 24 }">
      <Label text="L/R  NAVEGAR    ENTER  CONFIRMAR" :el-style="footerStyle" :anchor="{ Width: 560 }" />
      <Label text="ESC  SALIR" :el-style="hintStyle" :anchor="{ Width: 410 }" />
    </Group>
  </Group>

  <!-- Animacion CRT: AssetImage con Visible. No usamos Background ni Anchor
       explicitos en los elementos que cambian: Vuetale los reenvia al marcar
       dirty y el cliente rechaza Anchor.Full como Number/PropertyColor. -->
  <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtNegro.png'" :visible="overlayFase === 'negro'" />
  <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtFade1.png'" :visible="overlayFase === 'fade1'" />
  <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtFade2.png'" :visible="overlayFase === 'fade2'" />
  <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtFade3.png'" :visible="overlayFase === 'fade3'" />
  <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtFade4.png'" :visible="overlayFase === 'fade4'" />
  <Group :visible="lineaFase === 1" layout-mode="Middle">
    <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtLinea1.png'" :anchor="{ Width: 220, Height: 4 }" />
  </Group>
  <Group :visible="lineaFase === 2" layout-mode="Middle">
    <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtLinea2.png'" :anchor="{ Width: 620, Height: 4 }" />
  </Group>
  <Group :visible="lineaFase === 3" layout-mode="Middle">
    <AssetImage :asset-path="'UI/Custom/Common/Crt/CrtLinea3.png'" :anchor="{ Width: 1020, Height: 4 }" />
  </Group>
</template>
