import { defineComponent as O, computed as i, openBlock as s, createBlock as B, unref as o, withCtx as D, createElementVNode as t, createElementBlock as d, Fragment as E, renderList as _, createCommentVNode as T } from "vue";
import { useData as c } from "vt:@core/composables/useData";
import R from "../components/CyberShell.vue.js";
import { label as u, CYBER as n, buttonStyle as y, panelBg as L, panelSoftBg as M, captionStyle as P } from "../cyber.js";
const F = {
  "layout-mode": "Left",
  anchor: { Height: 32 }
}, U = ["el-style"], $ = ["text", "el-style"], j = ["background"], V = ["background"], Y = {
  "layout-mode": "Left",
  anchor: { Height: 56 }
}, q = ["asset-path"], w = {
  "layout-mode": "Top",
  anchor: { Width: 156, Height: 56 }
}, J = ["text", "el-style"], Z = ["text", "el-style"], z = ["text", "el-style", "onActivating"], K = {
  key: 0,
  anchor: { Width: 8 }
}, Q = ["background"], X = ["asset-path", "visible"], tt = {
  "layout-mode": "Top",
  anchor: { Width: 840, Height: 96 }
}, et = ["text", "el-style"], ot = ["text", "el-style"], nt = ["text", "el-style"], lt = {
  "layout-mode": "Left",
  anchor: { Height: 42 }
}, at = ["el-style"], it = ["el-style"], rt = ["el-style"], pt = /* @__PURE__ */ O({
  __name: "SeleccionPage",
  setup(st) {
    const m = c("starters", []), h = c("seleccionada", ""), f = c("mensaje", "SISTEMA ESPERANDO SELECCION"), H = c("onAction", () => {
    }), v = c("animarEntrada", !0), r = i(() => m.value.find((g) => g.id === h.value));
    function p(g, e) {
      const a = H.value;
      typeof a == "function" && a(JSON.stringify({ action: g, value: e }));
    }
    const x = i(
      () => u(n.gold, 11, "Start", !0, { LetterSpacing: 1 })
    ), G = i(() => u(n.text, 11, "Start", !0)), b = i(() => u(n.muted, 10, "Start", !1, { Wrap: !0 })), I = i(() => u(n.text, 18, "Start", !0, { LetterSpacing: 1 })), S = i(() => u(n.cyan, 10, "Start", !1, { LetterSpacing: 1 })), C = i(() => u(n.muted, 9, "Start", !1, { LetterSpacing: 1 })), A = y(n.cyan), N = y(n.gold, "button_gold"), W = y(n.danger, "button_danger");
    return (g, e) => (s(), B(R, {
      animate: o(v),
      title: "DIGI-EGG SYSTEM",
      subtitle: "INICIALIZACION // ELIGE TU COMPANERO",
      nav: !1,
      status: "PRIMER ENLACE"
    }, {
      default: D(() => [
        t("Group", F, [
          t("Label", {
            text: "UNIDADES DISPONIBLES",
            "el-style": x.value,
            anchor: { Width: 400 }
          }, null, 8, U),
          t("Label", {
            text: o(f),
            "el-style": S.value,
            anchor: { Width: 258, Height: 24 }
          }, null, 8, $)
        ]),
        t("Group", {
          anchor: { Height: 2 },
          background: { Color: o(n).cyan }
        }, null, 8, j),
        e[8] || (e[8] = t("Group", { anchor: { Height: 6 } }, null, -1)),
        (s(), d(E, null, _([0, 1], (a) => t("Group", {
          key: a,
          "layout-mode": "Left",
          anchor: { Height: 120 }
        }, [
          (s(!0), d(E, null, _(o(m).slice(a * 4, a * 4 + 4), (l, k) => (s(), d("Group", {
            key: l.id,
            "layout-mode": "Left",
            anchor: { Height: 120 }
          }, [
            (s(), d("Group", {
              key: l.id + (o(h) === l.id ? "-sel" : "-idle"),
              "layout-mode": "Top",
              anchor: { Width: 230, Height: 104 },
              background: o(h) === l.id ? o(L) : o(M),
              padding: { Full: 6 }
            }, [
              t("Group", Y, [
                t("AssetImage", {
                  "asset-path": l.imagen,
                  anchor: { Width: 56, Height: 56 }
                }, null, 8, q),
                e[2] || (e[2] = t("Group", { anchor: { Width: 6 } }, null, -1)),
                t("Group", w, [
                  t("Label", {
                    text: l.nombre,
                    "el-style": G.value,
                    anchor: { Height: 20 }
                  }, null, 8, J),
                  t("Label", {
                    text: l.elemento,
                    "el-style": o(P)(o(n).cyan),
                    anchor: { Height: 18 }
                  }, null, 8, Z)
                ])
              ]),
              e[3] || (e[3] = t("Group", { anchor: { Height: 4 } }, null, -1)),
              t("TextButton", {
                text: o(h) === l.id ? "SELECCIONADO" : "ELEGIR",
                anchor: { Height: 32 },
                "el-style": o(A),
                onActivating: (ut) => p("seleccionar", l.id)
              }, null, 40, z)
            ], 8, V)),
            k < 3 ? (s(), d("Group", K)) : T("", !0)
          ]))), 128))
        ])), 64)),
        e[9] || (e[9] = t("Group", { anchor: { Height: 6 } }, null, -1)),
        t("Group", {
          "layout-mode": "Left",
          anchor: { Height: 120 },
          background: o(L),
          padding: { Full: 8 }
        }, [
          t("AssetImage", {
            "asset-path": r.value?.imagen || "UI/Custom/png_Digis/Botamon.png",
            visible: !!r.value,
            anchor: { Width: 96, Height: 96 }
          }, null, 8, X),
          e[5] || (e[5] = t("Group", { anchor: { Width: 10 } }, null, -1)),
          t("Group", tt, [
            t("Label", {
              text: r.value?.nombre || "SIN SELECCION",
              "el-style": I.value,
              anchor: { Height: 32 }
            }, null, 8, et),
            t("Label", {
              text: r.value ? "ELEMENTO // " + r.value.elemento : "Elige una unidad para iniciar el enlace.",
              "el-style": S.value,
              anchor: { Height: 18 }
            }, null, 8, ot),
            e[4] || (e[4] = t("Group", { anchor: { Height: 4 } }, null, -1)),
            t("Label", {
              text: r.value?.descripcion || "Los datos del Digimon apareceran aqui.",
              "el-style": b.value,
              anchor: { Height: 42 }
            }, null, 8, nt)
          ])
        ], 8, Q),
        e[10] || (e[10] = t("Group", { anchor: { Height: 8 } }, null, -1)),
        t("Group", lt, [
          t("TextButton", {
            text: "CONFIRMAR ENLACE",
            anchor: { Width: 220, Height: 34 },
            "el-style": o(N),
            onActivating: e[0] || (e[0] = (a) => p("confirmar"))
          }, null, 40, at),
          e[6] || (e[6] = t("Group", { anchor: { Width: 8 } }, null, -1)),
          t("TextButton", {
            text: "CANCELAR",
            anchor: { Width: 160, Height: 34 },
            "el-style": o(W),
            onActivating: e[1] || (e[1] = (a) => p("cancelar"))
          }, null, 40, it),
          e[7] || (e[7] = t("Group", { anchor: { Width: 8 } }, null, -1)),
          t("Label", {
            text: "8 UNIDADES // 1 ELECCION",
            "el-style": C.value,
            anchor: { Width: 260, Height: 34 }
          }, null, 8, rt)
        ])
      ]),
      _: 1
    }, 8, ["animate"]));
  }
});
export {
  pt as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiU2VsZWNjaW9uUGFnZS52dWUuanMiLCJzb3VyY2VzIjpbXSwic291cmNlc0NvbnRlbnQiOltdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7In0=
