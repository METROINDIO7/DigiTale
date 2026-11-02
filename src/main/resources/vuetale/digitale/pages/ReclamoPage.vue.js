import { defineComponent as E, computed as r, openBlock as h, createBlock as O, withCtx as N, createElementVNode as t, unref as o, createElementBlock as g, Fragment as $, renderList as U } from "vue";
import { useData as s } from "vt:@core/composables/useData";
import M from "../components/CyberShell.vue.js";
import { label as u, CYBER as l, buttonStyle as d, panelBg as P, rowBg as D } from "../cyber.js";
const j = {
  "layout-mode": "Middle",
  anchor: { Height: 470 }
}, q = ["background"], Q = {
  "layout-mode": "Left",
  anchor: { Height: 36 }
}, w = ["el-style"], F = ["el-style"], V = ["visible"], z = ["text", "el-style"], J = {
  "layout-mode": "Middle",
  anchor: { Height: 70 }
}, Y = ["asset-path", "visible"], K = {
  "layout-mode": "Middle",
  anchor: { Height: 44 }
}, X = {
  "layout-mode": "Left",
  anchor: { Width: 466, Height: 40 }
}, Z = ["el-style", "visible"], tt = ["el-style", "visible"], et = ["el-style", "visible"], ot = ["el-style"], nt = ["visible"], lt = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, it = ["el-style"], st = ["text", "el-style"], at = ["background"], rt = ["asset-path", "visible"], ut = {
  "layout-mode": "Top",
  anchor: { Width: 340, Height: 30 }
}, dt = ["text", "el-style"], ht = ["text", "el-style"], ct = ["el-style", "visible", "onActivating"], gt = ["el-style", "visible", "onActivating"], yt = {
  "layout-mode": "Left",
  anchor: { Height: 40 }
}, pt = ["text", "el-style"], bt = ["el-style"], Ht = /* @__PURE__ */ E({
  __name: "ReclamoPage",
  setup(vt) {
    const a = s("tab", 0), c = s("tieneToken", !1), A = s("tokenTexto", ""), y = s("imagen", ""), H = s("banco", []), f = s("conteo", ""), B = s("mensaje", ""), W = s("onAction", () => {
    });
    function i(x, e) {
      const n = W.value;
      typeof n == "function" && n(JSON.stringify({ action: x, value: e }));
    }
    const G = r(() => u(l.cyan, 11, "Center", !0, { Wrap: !0 })), S = r(() => u(l.muted, 9, "Center")), C = r(() => u(l.gold, 10, "Start", !0, { LetterSpacing: 1 })), L = r(() => u(l.text, 10, "Start", !0)), p = r(() => u(l.muted, 9, "Start")), R = r(() => u(l.gold, 9, "Start", !1, { Wrap: !0 })), b = d(l.gold, "button_gold"), v = d(l.cyan), _ = d(l.green), I = d(l.cyan), m = d(l.danger, "button_danger"), T = d(l.danger, "button_danger");
    return (x, e) => (h(), O(M, {
      title: "RECLAMO DE CRIAS",
      subtitle: "INCUBACION // MESA",
      nav: !1,
      animate: !1,
      fondo: !1,
      status: "INCUBADORA"
    }, {
      default: N(() => [
        t("Group", j, [
          t("Group", {
            "layout-mode": "Top",
            anchor: { Width: 760, Height: 460 },
            background: o(P),
            padding: { Full: 12 }
          }, [
            t("Group", Q, [
              (h(), g("TextButton", {
                key: "tab-rec-" + (o(a) === 0),
                text: "RECLAMAR",
                anchor: { Width: 362, Height: 34 },
                "el-style": o(a) === 0 ? o(b) : o(v),
                onActivating: e[0] || (e[0] = (n) => i("tablistas"))
              }, null, 40, w)),
              e[6] || (e[6] = t("Group", { anchor: { Width: 8 } }, null, -1)),
              (h(), g("TextButton", {
                key: "tab-bco-" + (o(a) === 1),
                text: "BANCO PC",
                anchor: { Width: 362, Height: 34 },
                "el-style": o(a) === 1 ? o(b) : o(v),
                onActivating: e[1] || (e[1] = (n) => i("tabbanco"))
              }, null, 40, F))
            ]),
            e[13] || (e[13] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Group", {
              "layout-mode": "Top",
              anchor: { Height: 320 },
              visible: o(a) === 0
            }, [
              t("Label", {
                text: o(A),
                "el-style": G.value,
                anchor: { Height: 50 }
              }, null, 8, z),
              t("Group", J, [
                t("AssetImage", {
                  "asset-path": o(y) || "UI/Custom/png_Digis/Botamon.png",
                  visible: o(c) && !!o(y),
                  anchor: { Width: 64, Height: 64 }
                }, null, 8, Y)
              ]),
              t("Group", K, [
                t("Group", X, [
                  t("TextButton", {
                    text: "EQUIPO",
                    anchor: { Width: 150, Height: 40 },
                    "el-style": o(_),
                    visible: o(c),
                    onActivating: e[2] || (e[2] = (n) => i("equipo"))
                  }, null, 40, Z),
                  e[7] || (e[7] = t("Group", { anchor: { Width: 8 } }, null, -1)),
                  t("TextButton", {
                    text: "BANCO",
                    anchor: { Width: 150, Height: 40 },
                    "el-style": o(I),
                    visible: o(c),
                    onActivating: e[3] || (e[3] = (n) => i("banco"))
                  }, null, 40, tt),
                  e[8] || (e[8] = t("Group", { anchor: { Width: 8 } }, null, -1)),
                  t("TextButton", {
                    text: "LIBERAR",
                    anchor: { Width: 150, Height: 40 },
                    "el-style": o(m),
                    visible: o(c),
                    onActivating: e[4] || (e[4] = (n) => i("liberar"))
                  }, null, 40, et)
                ])
              ]),
              t("Label", {
                text: "EQUIPO = al equipo  |  BANCO = a la caja  |  LIBERAR = descartar",
                "el-style": S.value,
                anchor: { Height: 20 }
              }, null, 8, ot)
            ], 8, V),
            t("Group", {
              "layout-mode": "Top",
              anchor: { Height: 320 },
              visible: o(a) === 1
            }, [
              t("Group", lt, [
                t("Label", {
                  text: "BANCO PC // RETIRAR",
                  "el-style": C.value,
                  anchor: { Width: 500 }
                }, null, 8, it),
                t("Label", {
                  text: o(f),
                  "el-style": p.value,
                  anchor: { Width: 224 }
                }, null, 8, st)
              ]),
              e[11] || (e[11] = t("Group", { anchor: { Height: 4 } }, null, -1)),
              (h(!0), g($, null, U(o(H), (n) => (h(), g("Group", {
                key: n.indice,
                "layout-mode": "Left",
                anchor: { Height: 34 },
                background: o(D),
                padding: { Horizontal: 6, Vertical: 2 }
              }, [
                t("AssetImage", {
                  "asset-path": n.imagen || "UI/Custom/png_Digis/Botamon.png",
                  visible: n.ocupado && !!n.imagen,
                  anchor: { Width: 28, Height: 28 }
                }, null, 8, rt),
                e[9] || (e[9] = t("Group", { anchor: { Width: 6 } }, null, -1)),
                t("Group", ut, [
                  t("Label", {
                    text: n.nombre,
                    "el-style": L.value,
                    anchor: { Height: 17 }
                  }, null, 8, dt),
                  t("Label", {
                    text: n.info,
                    "el-style": p.value,
                    anchor: { Height: 13 }
                  }, null, 8, ht)
                ]),
                t("TextButton", {
                  text: "EQUIPO",
                  anchor: { Width: 82, Height: 30 },
                  "el-style": o(_),
                  visible: n.ocupado,
                  onActivating: (k) => i("bequipo", n.indice)
                }, null, 40, ct),
                e[10] || (e[10] = t("Group", { anchor: { Width: 6 } }, null, -1)),
                t("TextButton", {
                  text: "LIBERAR",
                  anchor: { Width: 78, Height: 30 },
                  "el-style": o(m),
                  visible: n.ocupado,
                  onActivating: (k) => i("bliberar", n.indice)
                }, null, 40, gt)
              ], 8, at))), 128))
            ], 8, nt),
            e[14] || (e[14] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Group", yt, [
              t("Label", {
                text: o(B),
                "el-style": R.value,
                anchor: { Width: 520, Height: 40 }
              }, null, 8, pt),
              e[12] || (e[12] = t("Group", { anchor: { Width: 6 } }, null, -1)),
              t("TextButton", {
                text: "CERRAR",
                anchor: { Width: 200, Height: 36 },
                "el-style": o(T),
                onActivating: e[5] || (e[5] = (n) => i("cerrar"))
              }, null, 40, bt)
            ])
          ], 8, q)
        ])
      ]),
      _: 1
    }));
  }
});
export {
  Ht as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiUmVjbGFtb1BhZ2UudnVlLmpzIiwic291cmNlcyI6W10sInNvdXJjZXNDb250ZW50IjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OyJ9
