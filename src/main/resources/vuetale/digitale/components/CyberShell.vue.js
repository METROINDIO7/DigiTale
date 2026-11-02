import { defineComponent as B, computed as u, ref as C, onMounted as F, onBeforeUnmount as W, openBlock as h, createElementBlock as c, Fragment as b, createCommentVNode as p, createElementVNode as e, unref as n, renderList as k, renderSlot as N } from "vue";
import { captionStyle as g, titleStyle as M, label as m, CYBER as a, headerBg as O, panelSoftBg as Y, navActiveStyle as D, navStyle as P } from "../cyber.js";
const V = {
  key: 0,
  "asset-path": "UI/FondoUI.png"
}, q = ["background"], K = ["background"], $ = {
  "layout-mode": "Top",
  anchor: { Width: 700, Height: 52 }
}, z = ["text", "el-style"], Q = ["text", "el-style"], Z = {
  "layout-mode": "Top",
  anchor: { Width: 240, Height: 52 }
}, j = ["el-style"], w = ["text", "el-style"], J = {
  "layout-mode": "Left",
  anchor: { Height: 480 }
}, X = ["background"], ee = ["el-style"], te = ["text", "el-style", "onActivating"], oe = ["el-style"], le = ["el-style"], ne = ["el-style"], ae = {
  key: 1,
  anchor: { Width: 10 }
}, se = ["anchor"], ie = {
  "layout-mode": "Left",
  anchor: { Height: 24 }
}, ue = ["el-style"], de = ["el-style"], re = ["visible"], he = ["visible"], ce = ["visible"], me = ["visible"], ge = ["visible"], ve = ["visible"], ye = ["visible"], pe = ["visible"], Ie = /* @__PURE__ */ B({
  __name: "CyberShell",
  props: {
    title: { default: "DIGITALE" },
    subtitle: { default: "DIGITAL NETWORK" },
    active: { default: "equipo" },
    nav: { type: Boolean, default: !0 },
    status: { default: "LINK ESTABLE" },
    animate: { type: Boolean, default: !0 },
    fondo: { type: Boolean, default: !0 }
  },
  emits: ["nav"],
  setup(o, { emit: I }) {
    const v = o, S = I, L = [
      { id: "equipo", label: "EQUIPO", code: "01" },
      { id: "crianza", label: "CRIANZA", code: "02" },
      { id: "batalla", label: "BATALLA", code: "03" },
      { id: "perfil", label: "PERFIL", code: "04" }
    ], _ = u(() => v.nav ? 816 : 972), l = C(
      v.animate ? "negro" : "none"
    ), s = C(0);
    let y = [];
    function f() {
      for (const r of y) clearTimeout(r);
      y = [];
    }
    function i(r, t) {
      y.push(setTimeout(t, r));
    }
    function H() {
      f(), l.value = "negro", s.value = 0, i(80, () => s.value = 1), i(160, () => s.value = 2), i(240, () => s.value = 3), i(300, () => {
        s.value = 0, l.value = "fade1";
      }), i(360, () => l.value = "fade2"), i(420, () => l.value = "fade3"), i(480, () => l.value = "fade4"), i(540, () => l.value = "none");
    }
    F(() => {
      v.animate && H();
    }), W(f);
    const A = u(() => g(a.cyan)), E = u(() => M(a.cyan)), G = u(
      () => m(a.muted, 9, "Start", !1, { LetterSpacing: 2, RenderUppercase: !0 })
    ), T = u(() => m(a.gold, 11, "End", !0, { LetterSpacing: 1 })), x = u(() => m(a.muted, 9, "Start", !1, { LetterSpacing: 2 })), R = u(() => m(a.muted, 9, "Start", !1, { LetterSpacing: 1 })), U = u(() => m(a.muted, 9, "End", !1, { LetterSpacing: 1 }));
    return (r, t) => (h(), c(b, null, [
      o.fondo ? (h(), c("AssetImage", V)) : p("", !0),
      e("Group", {
        "layout-mode": "Top",
        anchor: { Width: 1020, Height: 620 },
        background: o.fondo ? { Color: "#04070dcc" } : { Color: "#04070d" },
        padding: { Full: 12 }
      }, [
        e("Group", {
          "layout-mode": "Left",
          anchor: { Height: 68 },
          background: n(O),
          padding: { Full: 8 }
        }, [
          e("Group", $, [
            e("Label", {
              text: o.title,
              "el-style": E.value,
              anchor: { Height: 32 }
            }, null, 8, z),
            e("Label", {
              text: o.subtitle,
              "el-style": G.value,
              anchor: { Height: 20 }
            }, null, 8, Q)
          ]),
          e("Group", Z, [
            e("Label", {
              text: "/// CYBER SYSTEM",
              "el-style": A.value,
              anchor: { Height: 24 }
            }, null, 8, j),
            e("Label", {
              text: o.status,
              "el-style": T.value,
              anchor: { Height: 24 }
            }, null, 8, w)
          ])
        ], 8, K),
        t[2] || (t[2] = e("Group", { anchor: { Height: 6 } }, null, -1)),
        e("Group", J, [
          o.nav ? (h(), c("Group", {
            key: 0,
            "layout-mode": "Top",
            anchor: { Width: 170, Height: 480 },
            background: n(Y),
            padding: { Full: 8 }
          }, [
            e("Label", {
              text: "SECCIONES",
              "el-style": x.value,
              anchor: { Height: 20 }
            }, null, 8, ee),
            t[0] || (t[0] = e("Group", { anchor: { Height: 4 } }, null, -1)),
            (h(), c(b, null, k(L, (d) => e("TextButton", {
              key: d.id + (o.active === d.id ? "-on" : "-off"),
              text: (o.active === d.id ? "> " : "  ") + d.code + "  " + d.label,
              anchor: { Height: 40 },
              "el-style": o.active === d.id ? n(D) : n(P),
              onActivating: (fe) => S("nav", d.id)
            }, null, 40, te)), 64)),
            t[1] || (t[1] = e("Group", { anchor: { Height: 10 } }, null, -1)),
            e("Label", {
              text: "DIGIMON STORY",
              "el-style": n(g)(n(a).muted),
              anchor: { Height: 16 }
            }, null, 8, oe),
            e("Label", {
              text: "CYBER SLEUTH",
              "el-style": n(g)(n(a).cyan),
              anchor: { Height: 16 }
            }, null, 8, le),
            e("Label", {
              text: "INSPIRED UI",
              "el-style": n(g)(n(a).muted),
              anchor: { Height: 16 }
            }, null, 8, ne)
          ], 8, X)) : p("", !0),
          o.nav ? (h(), c("Group", ae)) : p("", !0),
          e("Group", {
            "layout-mode": "Top",
            anchor: { Width: _.value, Height: 480 }
          }, [
            N(r.$slots, "default")
          ], 8, se)
        ]),
        t[3] || (t[3] = e("Group", { anchor: { Height: 8 } }, null, -1)),
        e("Group", ie, [
          e("Label", {
            text: "L/R  NAVEGAR    ENTER  CONFIRMAR",
            "el-style": R.value,
            anchor: { Width: 560 }
          }, null, 8, ue),
          e("Label", {
            text: "ESC  SALIR",
            "el-style": U.value,
            anchor: { Width: 410 }
          }, null, 8, de)
        ])
      ], 8, q),
      e("AssetImage", {
        "asset-path": "UI/Custom/Common/Crt/CrtNegro.png",
        visible: l.value === "negro"
      }, null, 8, re),
      e("AssetImage", {
        "asset-path": "UI/Custom/Common/Crt/CrtFade1.png",
        visible: l.value === "fade1"
      }, null, 8, he),
      e("AssetImage", {
        "asset-path": "UI/Custom/Common/Crt/CrtFade2.png",
        visible: l.value === "fade2"
      }, null, 8, ce),
      e("AssetImage", {
        "asset-path": "UI/Custom/Common/Crt/CrtFade3.png",
        visible: l.value === "fade3"
      }, null, 8, me),
      e("AssetImage", {
        "asset-path": "UI/Custom/Common/Crt/CrtFade4.png",
        visible: l.value === "fade4"
      }, null, 8, ge),
      e("Group", {
        visible: s.value === 1,
        "layout-mode": "Middle"
      }, [...t[4] || (t[4] = [
        e("AssetImage", {
          "asset-path": "UI/Custom/Common/Crt/CrtLinea1.png",
          anchor: { Width: 220, Height: 4 }
        }, null, -1)
      ])], 8, ve),
      e("Group", {
        visible: s.value === 2,
        "layout-mode": "Middle"
      }, [...t[5] || (t[5] = [
        e("AssetImage", {
          "asset-path": "UI/Custom/Common/Crt/CrtLinea2.png",
          anchor: { Width: 620, Height: 4 }
        }, null, -1)
      ])], 8, ye),
      e("Group", {
        visible: s.value === 3,
        "layout-mode": "Middle"
      }, [...t[6] || (t[6] = [
        e("AssetImage", {
          "asset-path": "UI/Custom/Common/Crt/CrtLinea3.png",
          anchor: { Width: 1020, Height: 4 }
        }, null, -1)
      ])], 8, pe)
    ], 64));
  }
});
export {
  Ie as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiQ3liZXJTaGVsbC52dWUuanMiLCJzb3VyY2VzIjpbXSwic291cmNlc0NvbnRlbnQiOltdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7In0=
