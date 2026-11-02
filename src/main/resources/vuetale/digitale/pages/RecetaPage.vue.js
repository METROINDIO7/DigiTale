import { defineComponent as T, computed as l, openBlock as i, createBlock as D, withCtx as W, createElementVNode as t, unref as n, createElementBlock as d, Fragment as I, renderList as N } from "vue";
import { useData as s } from "vt:@core/composables/useData";
import O from "../components/CyberShell.vue.js";
import { label as r, CYBER as o, buttonStyle as h, panelBg as M } from "../cyber.js";
const $ = {
  "layout-mode": "Middle",
  anchor: { Height: 470 }
}, j = ["background"], F = ["text", "el-style"], U = ["text", "el-style"], q = ["background"], w = ["el-style"], J = ["text", "el-style"], P = ["text", "el-style"], Q = ["text", "el-style"], V = ["text", "el-style"], Y = ["text", "el-style"], z = {
  "layout-mode": "Middle",
  anchor: { Height: 40 }
}, K = {
  "layout-mode": "Left",
  anchor: { Width: 412, Height: 38 }
}, X = ["el-style"], Z = ["el-style"], st = /* @__PURE__ */ T({
  __name: "RecetaPage",
  setup(tt) {
    const g = s("huevo", ""), y = s("familia", ""), _ = s("ingredientes", []), m = s("entorno", ""), p = s("energia", ""), f = s("nace", ""), x = s("mensaje", ""), H = s("onAction", () => {
    });
    function u(c) {
      const e = H.value;
      typeof e == "function" && e(JSON.stringify({ action: c }));
    }
    const b = l(() => r(o.cyan, 15, "Center", !0)), v = l(() => r(o.muted, 10, "Center")), S = l(() => r(o.gold, 11, "Center", !0, { LetterSpacing: 1 })), C = l(() => r(o.text, 10, "Center")), A = l(() => r(o.text, 10, "Center", !1, { Wrap: !0 })), E = l(() => r(o.gold, 10, "Center")), B = l(() => r(o.green, 11, "Center", !0)), G = l(() => r(o.gold, 9, "Center", !1, { Wrap: !0 })), L = h(o.gold, "button_gold"), k = h(o.danger, "button_danger");
    return (c, e) => (i(), D(O, {
      title: "DATO INCUBACION",
      subtitle: "RECETA // BASE DE DATOS",
      nav: !1,
      animate: !1,
      fondo: !1,
      status: "DIGITAMA"
    }, {
      default: W(() => [
        t("Group", $, [
          t("Group", {
            "layout-mode": "Top",
            anchor: { Width: 720, Height: 450 },
            background: n(M),
            padding: { Full: 14 }
          }, [
            t("Label", {
              text: n(g),
              "el-style": b.value,
              anchor: { Height: 24 }
            }, null, 8, F),
            t("Label", {
              text: n(y),
              "el-style": v.value,
              anchor: { Height: 18 }
            }, null, 8, U),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: n(o).gold }
            }, null, 8, q),
            e[3] || (e[3] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            t("Label", {
              text: "INGREDIENTES",
              "el-style": S.value,
              anchor: { Height: 20 }
            }, null, 8, w),
            (i(!0), d(I, null, N(n(_), (a, R) => (i(), d("Label", {
              key: R,
              text: "- " + a,
              "el-style": C.value,
              anchor: { Height: 18 }
            }, null, 8, J))), 128)),
            e[4] || (e[4] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            t("Label", {
              text: n(m),
              "el-style": A.value,
              anchor: { Height: 32 }
            }, null, 8, P),
            t("Label", {
              text: n(p),
              "el-style": E.value,
              anchor: { Height: 18 }
            }, null, 8, Q),
            t("Label", {
              text: n(f),
              "el-style": B.value,
              anchor: { Height: 22 }
            }, null, 8, V),
            e[5] || (e[5] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Label", {
              text: n(x),
              "el-style": G.value,
              anchor: { Height: 20 }
            }, null, 8, Y),
            e[6] || (e[6] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Group", z, [
              t("Group", K, [
                t("TextButton", {
                  text: "DESBLOQUEAR",
                  anchor: { Width: 240, Height: 38 },
                  "el-style": n(L),
                  onActivating: e[0] || (e[0] = (a) => u("desbloquear"))
                }, null, 40, X),
                e[2] || (e[2] = t("Group", { anchor: { Width: 12 } }, null, -1)),
                t("TextButton", {
                  text: "CERRAR",
                  anchor: { Width: 160, Height: 38 },
                  "el-style": n(k),
                  onActivating: e[1] || (e[1] = (a) => u("cerrar"))
                }, null, 40, Z)
              ])
            ])
          ], 8, j)
        ])
      ]),
      _: 1
    }));
  }
});
export {
  st as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiUmVjZXRhUGFnZS52dWUuanMiLCJzb3VyY2VzIjpbXSwic291cmNlc0NvbnRlbnQiOltdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OyJ9
