const t = {
  bg: "#04070d",
  panel: "#07111d",
  panelAlt: "#0a1826",
  text: "#d7ecff",
  muted: "#6f8ca4",
  cyan: "#38d9ff",
  magenta: "#ff4fa3",
  gold: "#ffc857",
  green: "#5dffb0",
  danger: "#ff5f6d",
  white: "#ffffff"
}, e = (r, a) => ({
  TexturePath: `Common/Cyber/${r}.png`,
  Border: a
}), d = e("panel_deep", 8), S = e("panel", 8), g = e("panel_soft", 8), f = e("row", 5), i = e("header", 8), u = 1.25;
function n(r, a, o = "Start", l = !1, c = {}) {
  return {
    TextColor: r,
    FontSize: Math.round(a * u),
    HorizontalAlignment: o,
    RenderBold: l,
    ...c
  };
}
function p(r = t.cyan) {
  return n(r, 20, "Start", !0, {
    LetterSpacing: 1,
    RenderUppercase: !0,
    OutlineColor: "#000000"
  });
}
function b(r = t.muted) {
  return n(r, 10, "Start", !1, { LetterSpacing: 1, RenderUppercase: !0 });
}
function s(r = t.cyan, a = "button") {
  return {
    Default: {
      Background: e(a, 6),
      LabelStyle: n(r, 12, "Center", !0, { LetterSpacing: 1 })
    },
    Hovered: {
      Background: e("button_hover", 6),
      LabelStyle: n(t.white, 12, "Center", !0, { LetterSpacing: 1 })
    },
    Pressed: {
      Background: e("button_active", 6),
      LabelStyle: n(t.gold, 12, "Center", !0, { LetterSpacing: 1 })
    }
  };
}
const L = {
  Default: {
    Background: { Color: "#00000000" },
    LabelStyle: n(t.muted, 12, "Start", !0, { LetterSpacing: 1 })
  },
  Hovered: {
    Background: e("button_hover", 6),
    LabelStyle: n(t.white, 12, "Start", !0, { LetterSpacing: 1 })
  },
  Pressed: {
    Background: e("button_active", 6),
    LabelStyle: n(t.gold, 12, "Start", !0, { LetterSpacing: 1 })
  }
}, y = {
  Default: {
    Background: e("button_active", 6),
    LabelStyle: n(t.gold, 12, "Start", !0, { LetterSpacing: 1 })
  },
  Hovered: {
    Background: e("button_hover", 6),
    LabelStyle: n(t.white, 12, "Start", !0, { LetterSpacing: 1 })
  }
};
export {
  t as CYBER,
  s as buttonStyle,
  b as captionStyle,
  i as headerBg,
  n as label,
  y as navActiveStyle,
  L as navStyle,
  S as panelBg,
  g as panelSoftBg,
  f as rowBg,
  d as shellBg,
  p as titleStyle
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiY3liZXIuanMiLCJzb3VyY2VzIjpbXSwic291cmNlc0NvbnRlbnQiOltdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OzsifQ==
