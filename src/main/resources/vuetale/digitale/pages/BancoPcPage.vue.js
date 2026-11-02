import { defineComponent as B, computed as a, openBlock as r, createBlock as C, unref as n, withCtx as I, createElementVNode as t, createElementBlock as u, Fragment as _, renderList as b } from "vue";
import { useData as i } from "vt:@core/composables/useData";
import D from "../components/CyberShell.vue.js";
import { label as s, CYBER as l, buttonStyle as h, panelBg as m, rowBg as H } from "../cyber.js";
const N = {
  "layout-mode": "Left",
  anchor: { Height: 26 }
}, O = ["el-style"], P = ["text", "el-style"], U = ["background"], F = {
  "layout-mode": "Left",
  anchor: { Height: 360 }
}, $ = ["background"], J = ["el-style"], V = ["background"], j = ["background"], q = ["asset-path", "visible"], w = {
  "layout-mode": "Top",
  anchor: { Width: 326, Height: 38 }
}, z = ["text", "el-style"], M = ["text", "el-style"], Q = ["el-style", "visible", "onActivating"], Y = ["background"], K = ["el-style"], X = ["background"], Z = ["background"], tt = ["asset-path", "visible"], et = {
  "layout-mode": "Top",
  anchor: { Width: 336, Height: 32 }
}, ot = ["text", "el-style"], nt = ["text", "el-style"], lt = ["el-style", "visible", "onActivating"], at = {
  "layout-mode": "Left",
  anchor: { Height: 40 }
}, it = ["text", "el-style"], st = ["el-style"], gt = /* @__PURE__ */ B({
  __name: "BancoPcPage",
  setup(rt) {
    const v = i("equipo", []), f = i("banco", []), A = i("conteo", ""), x = i("mensaje", ""), G = i("onAction", () => {
    }), S = i("animarEntrada", !0);
    function c(y, o) {
      const e = G.value;
      typeof e == "function" && e(JSON.stringify({ action: y, value: o }));
    }
    const d = a(
      () => s(l.gold, 11, "Start", !0, { LetterSpacing: 1 })
    ), k = a(() => s(l.cyan, 11, "End", !0)), g = a(() => s(l.text, 11, "Start", !0)), p = a(() => s(l.muted, 9, "Start")), L = a(
      () => s(l.gold, 10, "Start", !1, { Wrap: !0 })
    );
    a(() => s(l.muted, 9, "Start", !1, { LetterSpacing: 1 }));
    const W = h(l.gold, "button_gold"), E = h(l.cyan), R = h(l.danger, "button_danger");
    return (y, o) => (r(), C(D, {
      animate: n(S),
      title: "PC DIGITAL",
      subtitle: "CAJA DE DATOS // TRANSFERENCIA",
      nav: !1,
      status: "SISTEMA PC"
    }, {
      default: I(() => [
        t("Group", N, [
          t("Label", {
            text: "GESTION DE DATOS",
            "el-style": d.value,
            anchor: { Width: 400 }
          }, null, 8, O),
          t("Label", {
            text: n(A),
            "el-style": k.value,
            anchor: { Width: 560 }
          }, null, 8, P)
        ]),
        t("Group", {
          anchor: { Height: 2 },
          background: { Color: n(l).cyan }
        }, null, 8, U),
        o[7] || (o[7] = t("Group", { anchor: { Height: 8 } }, null, -1)),
        t("Group", F, [
          t("Group", {
            "layout-mode": "Top",
            anchor: { Width: 478, Height: 360 },
            background: n(m),
            padding: { Full: 8 }
          }, [
            t("Label", {
              text: "EQUIPO // GUARDAR",
              "el-style": d.value,
              anchor: { Height: 22 }
            }, null, 8, J),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: n(l).gold }
            }, null, 8, V),
            o[2] || (o[2] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            (r(!0), u(_, null, b(n(v), (e) => (r(), u("Group", {
              key: e.indice,
              "layout-mode": "Left",
              anchor: { Height: 44 },
              background: n(H),
              padding: { Horizontal: 6, Vertical: 3 }
            }, [
              t("AssetImage", {
                "asset-path": e.imagen || "UI/Custom/png_Digis/Botamon.png",
                visible: e.ocupado && !!e.imagen,
                anchor: { Width: 34, Height: 34 }
              }, null, 8, q),
              o[1] || (o[1] = t("Group", { anchor: { Width: 6 } }, null, -1)),
              t("Group", w, [
                t("Label", {
                  text: e.nombre,
                  "el-style": g.value,
                  anchor: { Height: 20 }
                }, null, 8, z),
                t("Label", {
                  text: e.info,
                  "el-style": p.value,
                  anchor: { Height: 16 }
                }, null, 8, M)
              ]),
              t("TextButton", {
                text: "GUARDAR",
                anchor: { Width: 90, Height: 34 },
                "el-style": n(W),
                visible: e.ocupado,
                onActivating: (T) => c("depositar", e.indice)
              }, null, 40, Q)
            ], 8, j))), 128))
          ], 8, $),
          o[5] || (o[5] = t("Group", { anchor: { Width: 8 } }, null, -1)),
          t("Group", {
            "layout-mode": "Top",
            anchor: { Width: 486, Height: 360 },
            background: n(m),
            padding: { Full: 8 }
          }, [
            t("Label", {
              text: "CAJA // RETIRAR",
              "el-style": d.value,
              anchor: { Height: 22 }
            }, null, 8, K),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: n(l).cyan }
            }, null, 8, X),
            o[4] || (o[4] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            (r(!0), u(_, null, b(n(f), (e) => (r(), u("Group", {
              key: e.indice,
              "layout-mode": "Left",
              anchor: { Height: 38 },
              background: n(H),
              padding: { Horizontal: 6, Vertical: 2 }
            }, [
              t("AssetImage", {
                "asset-path": e.imagen || "UI/Custom/png_Digis/Botamon.png",
                visible: e.ocupado && !!e.imagen,
                anchor: { Width: 30, Height: 30 }
              }, null, 8, tt),
              o[3] || (o[3] = t("Group", { anchor: { Width: 6 } }, null, -1)),
              t("Group", et, [
                t("Label", {
                  text: e.nombre,
                  "el-style": g.value,
                  anchor: { Height: 18 }
                }, null, 8, ot),
                t("Label", {
                  text: e.info,
                  "el-style": p.value,
                  anchor: { Height: 14 }
                }, null, 8, nt)
              ]),
              t("TextButton", {
                text: "RETIRAR",
                anchor: { Width: 88, Height: 30 },
                "el-style": n(E),
                visible: e.ocupado,
                onActivating: (T) => c("retirar", e.indice)
              }, null, 40, lt)
            ], 8, Z))), 128))
          ], 8, Y)
        ]),
        o[8] || (o[8] = t("Group", { anchor: { Height: 6 } }, null, -1)),
        t("Group", at, [
          t("Label", {
            text: n(x),
            "el-style": L.value,
            anchor: { Width: 700, Height: 40 }
          }, null, 8, it),
          o[6] || (o[6] = t("Group", { anchor: { Width: 10 } }, null, -1)),
          t("TextButton", {
            text: "CERRAR",
            anchor: { Width: 200, Height: 36 },
            "el-style": n(R),
            onActivating: o[0] || (o[0] = (e) => c("cerrar"))
          }, null, 40, st)
        ])
      ]),
      _: 1
    }, 8, ["animate"]));
  }
});
export {
  gt as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiQmFuY29QY1BhZ2UudnVlLmpzIiwic291cmNlcyI6W10sInNvdXJjZXNDb250ZW50IjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OyJ9
