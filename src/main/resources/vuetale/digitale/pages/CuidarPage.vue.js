import { defineComponent as V, computed as u, openBlock as r, createBlock as F, unref as l, withCtx as Z, createElementVNode as t, createElementBlock as d, Fragment as g, renderList as v } from "vue";
import { useData as c } from "vt:@core/composables/useData";
import w from "../components/CyberShell.vue.js";
import { label as h, CYBER as i, buttonStyle as y, rowBg as S, panelBg as z, captionStyle as Q } from "../cyber.js";
const j = {
  "layout-mode": "Left",
  anchor: { Height: 28 }
}, q = ["text", "el-style"], J = ["el-style"], Y = ["background"], K = ["text", "el-style"], X = ["background"], tt = ["text", "el-style"], et = ["el-style", "onActivating"], ot = ["el-style"], nt = ["background"], lt = {
  "layout-mode": "Left",
  anchor: { Height: 46 }
}, it = ["text", "el-style"], at = ["asset-path", "visible"], st = {
  "layout-mode": "Top",
  anchor: { Width: 306, Height: 46 }
}, ut = ["text", "el-style"], rt = ["text", "el-style"], dt = {
  "layout-mode": "Left",
  anchor: { Height: 26 }
}, ct = ["el-style", "visible", "onActivating"], ht = ["text", "el-style", "visible", "onActivating"], gt = ["el-style", "visible", "onActivating"], yt = ["el-style", "visible", "onActivating"], mt = ["background"], pt = ["el-style"], vt = ["text", "el-style"], _t = {
  "layout-mode": "Left",
  anchor: { Height: 38 }
}, xt = ["text", "el-style"], bt = ["el-style"], Et = /* @__PURE__ */ V({
  __name: "CuidarPage",
  setup(ft) {
    const L = c("titulo", "CRIANZA DE DIGIMON"), _ = c("slots", []), E = c("comidas", []), C = c("slotComida", -1), W = c("comidaPara", ""), G = c("info", "-"), I = c("onAction", () => {
    }), B = c("animarEntrada", !0), O = u(() => {
      const a = [], e = _.value;
      for (let o = 0; o < e.length; o += 2)
        a.push(e.slice(o, o + 2));
      return a;
    }), x = u(() => C.value >= 0), T = u(() => _.value.filter((a) => a.ocupado).length);
    function s(a, e) {
      const o = I.value;
      typeof o == "function" && o(JSON.stringify({ action: a, value: e }));
    }
    function k(a) {
      a === "equipo" ? s("volver") : a === "perfil" ? s("ir_perfil") : a === "batalla" && s("ir_batalla");
    }
    const b = u(
      () => h(i.gold, 11, "Start", !0, { LetterSpacing: 1 })
    ), D = u(() => h(i.muted, 10, "Start", !1, { Wrap: !0 })), f = u(() => h(i.text, 11, "Start", !0)), N = u(() => h(i.muted, 9, "Start")), R = u(() => h(i.text, 10, "Start", !1, { Wrap: !0 })), $ = u(() => h(i.muted, 9, "Start", !1, { LetterSpacing: 1 })), M = u(() => h(i.gold, 10, "Center", !0)), m = y(i.cyan), P = y(i.gold, "button_gold"), U = y(i.magenta, "button_magenta"), A = y(i.cyan);
    return (a, e) => (r(), F(w, {
      animate: l(B),
      title: "DIGIMON CARE",
      subtitle: "CRIANZA // CONTROL DE ESTADO",
      active: "crianza",
      status: "UNIDADES " + T.value + "/6",
      onNav: k
    }, {
      default: Z(() => [
        t("Group", j, [
          t("Label", {
            text: x.value ? "SELECCION DE ALIMENTO" : "CUIDA A TU EQUIPO",
            "el-style": b.value,
            anchor: { Width: 340 }
          }, null, 8, q),
          t("Label", {
            text: "H / E / L",
            "el-style": $.value,
            anchor: { Width: 158 }
          }, null, 8, J)
        ]),
        t("Group", {
          anchor: { Height: 2 },
          background: { Color: l(i).cyan }
        }, null, 8, Y),
        e[10] || (e[10] = t("Group", { anchor: { Height: 6 } }, null, -1)),
        x.value ? (r(), d(g, { key: 0 }, [
          t("Label", {
            text: l(W),
            "el-style": D.value,
            anchor: { Height: 22 }
          }, null, 8, K),
          (r(!0), d(g, null, v(l(E), (o) => (r(), d("Group", {
            key: o.index,
            "layout-mode": "Left",
            anchor: { Height: 46 },
            background: l(S),
            padding: { Horizontal: 8, Vertical: 4 }
          }, [
            t("Label", {
              text: o.nombre + " x" + o.cantidad + "   H+" + o.h + "  E+" + o.e,
              "el-style": f.value,
              anchor: { Width: 520, Height: 36 }
            }, null, 8, tt),
            t("TextButton", {
              text: "DAR",
              anchor: { Width: 120, Height: 36 },
              "el-style": l(P),
              onActivating: (H) => s("dar_comida", o.index)
            }, null, 40, et)
          ], 8, X))), 128)),
          e[2] || (e[2] = t("Group", { anchor: { Height: 10 } }, null, -1)),
          t("TextButton", {
            text: "VOLVER",
            anchor: { Width: 180, Height: 38 },
            "el-style": l(A),
            onActivating: e[0] || (e[0] = (o) => s("comida_volver"))
          }, null, 40, ot)
        ], 64)) : (r(), d(g, { key: 1 }, [
          (r(!0), d(g, null, v(O.value, (o, H) => (r(), d("Group", {
            key: H,
            "layout-mode": "Left",
            anchor: { Height: 102 }
          }, [
            (r(!0), d(g, null, v(o, (n) => (r(), d("Group", {
              key: n.index,
              "layout-mode": "Top",
              anchor: { Width: 400, Height: 100 },
              background: l(z),
              padding: { Full: 7 }
            }, [
              t("Group", lt, [
                t("Label", {
                  text: "0" + (n.index + 1),
                  "el-style": M.value,
                  anchor: { Width: 24, Height: 44 }
                }, null, 8, it),
                t("AssetImage", {
                  "asset-path": n.imagen || "UI/Custom/png_Digis/Botamon.png",
                  visible: n.ocupado && !!n.imagen,
                  anchor: { Width: 44, Height: 44 }
                }, null, 8, at),
                e[3] || (e[3] = t("Group", { anchor: { Width: 6 } }, null, -1)),
                t("Group", st, [
                  t("Label", {
                    text: n.ocupado ? n.nombre : "SIN DATOS",
                    "el-style": f.value,
                    anchor: { Height: 22 }
                  }, null, 8, ut),
                  t("Label", {
                    text: n.info,
                    "el-style": N.value,
                    anchor: { Height: 20 }
                  }, null, 8, rt)
                ])
              ]),
              e[7] || (e[7] = t("Group", { anchor: { Height: 4 } }, null, -1)),
              t("Group", dt, [
                t("TextButton", {
                  text: "COMER",
                  anchor: { Width: 72, Height: 30 },
                  "el-style": l(m),
                  visible: n.ocupado && !n.dormido,
                  onActivating: (p) => s("alimentar", n.index)
                }, null, 40, ct),
                e[4] || (e[4] = t("Group", { anchor: { Width: 4 } }, null, -1)),
                t("TextButton", {
                  text: n.dormido ? "DESP." : "DORMIR",
                  anchor: { Width: 72, Height: 30 },
                  "el-style": l(m),
                  visible: n.ocupado,
                  onActivating: (p) => s("dormir", n.index)
                }, null, 40, ht),
                e[5] || (e[5] = t("Group", { anchor: { Width: 4 } }, null, -1)),
                t("TextButton", {
                  text: "PASEO",
                  anchor: { Width: 68, Height: 30 },
                  "el-style": l(m),
                  visible: n.ocupado,
                  onActivating: (p) => s("jugar", n.index)
                }, null, 40, gt),
                e[6] || (e[6] = t("Group", { anchor: { Width: 4 } }, null, -1)),
                t("TextButton", {
                  text: "BANCO",
                  anchor: { Width: 68, Height: 30 },
                  "el-style": l(U),
                  visible: n.ocupado,
                  onActivating: (p) => s("banco", n.index)
                }, null, 40, yt)
              ])
            ], 8, nt))), 128)),
            e[8] || (e[8] = t("Group", { anchor: { Width: 8 } }, null, -1))
          ]))), 128)),
          e[9] || (e[9] = t("Group", { anchor: { Height: 6 } }, null, -1)),
          t("Group", {
            "layout-mode": "Top",
            anchor: { Height: 80 },
            background: l(S),
            padding: { Full: 8 }
          }, [
            t("Label", {
              text: "ULTIMA ACCION",
              "el-style": b.value,
              anchor: { Height: 20 }
            }, null, 8, pt),
            t("Label", {
              text: l(G),
              "el-style": R.value,
              anchor: { Height: 44 }
            }, null, 8, vt)
          ], 8, mt),
          t("Group", _t, [
            t("Label", {
              text: "SISTEMA DE CRIANZA // " + l(L),
              "el-style": l(Q)(l(i).muted),
              anchor: { Width: 330, Height: 38 }
            }, null, 8, xt),
            t("TextButton", {
              text: "VOLVER AL EQUIPO",
              anchor: { Width: 180, Height: 38 },
              "el-style": l(A),
              onActivating: e[1] || (e[1] = (o) => s("volver"))
            }, null, 40, bt)
          ])
        ], 64))
      ]),
      _: 1
    }, 8, ["animate", "status"]));
  }
});
export {
  Et as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiQ3VpZGFyUGFnZS52dWUuanMiLCJzb3VyY2VzIjpbXSwic291cmNlc0NvbnRlbnQiOltdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OzsifQ==
