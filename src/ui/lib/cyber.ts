import type { LabelStyle, PatchStyle, TextButtonStyle } from 'vuetale/types/elements'

export const CYBER = {
  bg: '#04070d',
  panel: '#07111d',
  panelAlt: '#0a1826',
  text: '#d7ecff',
  muted: '#6f8ca4',
  cyan: '#38d9ff',
  magenta: '#ff4fa3',
  gold: '#ffc857',
  green: '#5dffb0',
  danger: '#ff5f6d',
  white: '#ffffff',
} as const

/**
 * Vuetale inserta el markup de la pagina con `appendInline` dentro de
 * `Common/UI/Custom/Pages/HytaleRoot.ui`. Las rutas de textura NO se resuelven
 * desde `Common/` (el pack raiz) sino desde el arbol de UI.
 *
 * Se usa el prefijo `Common/` porque es el mismo convenio que usan los estilos
 * internos de Vuetale (`Common/Buttons/Primary.png`, `Common/core/...`) y que
 * Hytale resuelve contra `Common/UI/Custom/Common/`. Las texturas viven en
 * `src/main/resources/Common/UI/Custom/Common/Cyber/`.
 *
 * Con `UI/Custom/Cyber/...` el cliente buscaba
 * `Common/UI/Custom/UI/Custom/Cyber/...` y dibujaba una X roja (textura no
 * encontrada).
 */
const texture = (name: string, border: number): PatchStyle => ({
  TexturePath: `Common/Cyber/${name}.png`,
  Border: border,
})

export const shellBg: PatchStyle = texture('panel_deep', 8)
export const panelBg: PatchStyle = texture('panel', 8)
export const panelSoftBg: PatchStyle = texture('panel_soft', 8)
export const rowBg: PatchStyle = texture('row', 5)
export const headerBg: PatchStyle = texture('header', 8)

/**
 * Escala global de tipografia. Los tamanos de `label()` estan pensados para
 * una UI de 700x560; 1.25 los deja legibles en pantalla sin romper el layout.
 */
const FONT_SCALE = 1.25

export function label(
  color: string,
  size: number,
  align: LabelStyle['HorizontalAlignment'] = 'Start',
  bold = false,
  extra: LabelStyle = {},
): LabelStyle {
  return {
    TextColor: color,
    FontSize: Math.round(size * FONT_SCALE),
    HorizontalAlignment: align,
    RenderBold: bold,
    ...extra,
  }
}

export function titleStyle(color: string = CYBER.cyan): LabelStyle {
  return label(color, 20, 'Start', true, {
    LetterSpacing: 1,
    RenderUppercase: true,
    OutlineColor: '#000000',
  })
}

export function captionStyle(color: string = CYBER.muted): LabelStyle {
  return label(color, 10, 'Start', false, { LetterSpacing: 1, RenderUppercase: true })
}

export function buttonStyle(accent: string = CYBER.cyan, kind: string = 'button'): TextButtonStyle {
  return {
    Default: {
      Background: texture(kind, 6),
      LabelStyle: label(accent, 12, 'Center', true, { LetterSpacing: 1 }),
    },
    Hovered: {
      Background: texture('button_hover', 6),
      LabelStyle: label(CYBER.white, 12, 'Center', true, { LetterSpacing: 1 }),
    },
    Pressed: {
      Background: texture('button_active', 6),
      LabelStyle: label(CYBER.gold, 12, 'Center', true, { LetterSpacing: 1 }),
    },
  }
}

export const navStyle: TextButtonStyle = {
  Default: {
    Background: { Color: '#00000000' },
    LabelStyle: label(CYBER.muted, 12, 'Start', true, { LetterSpacing: 1 }),
  },
  Hovered: {
    Background: texture('button_hover', 6),
    LabelStyle: label(CYBER.white, 12, 'Start', true, { LetterSpacing: 1 }),
  },
  Pressed: {
    Background: texture('button_active', 6),
    LabelStyle: label(CYBER.gold, 12, 'Start', true, { LetterSpacing: 1 }),
  },
}

export const navActiveStyle: TextButtonStyle = {
  Default: {
    Background: texture('button_active', 6),
    LabelStyle: label(CYBER.gold, 12, 'Start', true, { LetterSpacing: 1 }),
  },
  Hovered: {
    Background: texture('button_hover', 6),
    LabelStyle: label(CYBER.white, 12, 'Start', true, { LetterSpacing: 1 }),
  },
}
