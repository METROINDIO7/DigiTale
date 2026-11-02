import { defineComponent as S, computed as i, openBlock as v, createBlock as H, unref as o, withCtx as B, createElementVNode as t } from "vue";
import { useData as c } from "vt:@core/composables/useData";
import O from "../components/CyberShell.vue.js";
import { label as r, CYBER as n, buttonStyle as h, rowBg as m } from "../cyber.js";
const T = {
  "layout-mode": "Left",
  anchor: { Height: 28 }
}, R = ["el-style"], D = ["text", "el-style"], G = ["background"], C = ["background"], N = ["text", "el-style"], I = ["text", "el-style"], W = ["text", "el-style"], k = ["text", "el-style"], P = {
  "layout-mode": "Left",
  anchor: { Height: 48 }
}, M = ["text", "el-style"], F = ["text", "el-style"], V = ["background"], $ = ["el-style"], w = ["el-style"], j = {
  "layout-mode": "Left",
  anchor: { Height: 38 }
}, J = ["el-style"], Y = ["el-style"], X = /* @__PURE__ */ S({
  __name: "BatallaPage",
  setup(q) {
    const l = c("batalla", {
      desafioAbierto: !1,
      modo: "Amistoso",
      activos: "",
      info: "",
      retoDe: ""
    }), p = c("onAction", () => {
    }), _ = c("animarEntrada", !0);
    function u(a, e) {
      const s = p.value;
      typeof s == "function" && s(JSON.stringify({ action: a, value: e }));
    }
    function f(a) {
      a !== "batalla" && u("ir_" + a);
    }
    const b = i(
      () => r(n.cyan, 13, "Start", !0, { LetterSpacing: 1 })
    ), g = i(
      () => r(n.gold, 11, "Start", !0, { LetterSpacing: 1 })
    ), d = i(() => r(n.muted, 10, "Start", !1, { Wrap: !0 })), A = i(() => r(n.text, 11, "Start", !1, { Wrap: !0 })), y = i(() => r(n.muted, 9, "Start", !1, { LetterSpacing: 1 })), E = h(n.gold, "button_gold"), x = h(n.cyan), L = h(n.danger, "button_danger");
    return (a, e) => (v(), H(O, {
      animate: o(_),
      title: "DIGIMON BATTLE",
      subtitle: "BATALLA // PVP",
      active: "batalla",
      status: "MODO " + o(l).modo,
      onNav: f
    }, {
      default: B(() => [
        t("Group", T, [
          t("Label", {
            text: "BATALLA PVP",
            "el-style": g.value,
            anchor: { Width: 330 }
          }, null, 8, R),
          t("Label", {
            text: o(l).desafioAbierto ? "ENLACE ABIERTO" : "ENLACE CERRADO",
            "el-style": y.value,
            anchor: { Width: 158 }
          }, null, 8, D)
        ]),
        t("Group", {
          anchor: { Height: 2 },
          background: { Color: o(n).magenta }
        }, null, 8, G),
        e[4] || (e[4] = t("Group", { anchor: { Height: 10 } }, null, -1)),
        t("Group", {
          "layout-mode": "Top",
          anchor: { Height: 180 },
          background: o(m),
          padding: { Full: 12 }
        }, [
          t("Label", {
            text: o(l).retoDe ? "RETO DE " + o(l).retoDe : "SIN RETOS PENDIENTES",
            "el-style": b.value,
            anchor: { Height: 28 }
          }, null, 8, N),
          t("Label", {
            text: "MODO: " + o(l).modo,
            "el-style": d.value,
            anchor: { Height: 26 }
          }, null, 8, I),
          t("Label", {
            text: o(l).activos,
            "el-style": A.value,
            anchor: { Height: 26 }
          }, null, 8, W),
          t("Label", {
            text: o(l).info,
            "el-style": d.value,
            anchor: { Height: 64 }
          }, null, 8, k)
        ], 8, C),
        e[5] || (e[5] = t("Group", { anchor: { Height: 10 } }, null, -1)),
        t("Group", P, [
          t("TextButton", {
            text: o(l).desafioAbierto ? "CERRAR DESAFIO" : "ABRIR DESAFIO",
            anchor: { Width: 320, Height: 44 },
            "el-style": o(E),
            onActivating: e[0] || (e[0] = (s) => u("desafio"))
          }, null, 40, M),
          e[3] || (e[3] = t("Group", { anchor: { Width: 10 } }, null, -1)),
          t("TextButton", {
            text: "MODO: " + o(l).modo,
            anchor: { Width: 300, Height: 44 },
            "el-style": o(x),
            onActivating: e[1] || (e[1] = (s) => u("modo"))
          }, null, 40, F)
        ]),
        e[6] || (e[6] = t("Group", { anchor: { Height: 10 } }, null, -1)),
        t("Group", {
          "layout-mode": "Top",
          anchor: { Height: 110 },
          background: o(m),
          padding: { Full: 12 }
        }, [
          t("Label", {
            text: "COMO RETAR",
            "el-style": g.value,
            anchor: { Height: 22 }
          }, null, 8, $),
          t("Label", {
            text: "Golpea con el sapotama a un rival con desafio abierto. Los Digimon salvajes inician combate al ser golpeados.",
            "el-style": d.value,
            anchor: { Height: 48 }
          }, null, 8, w)
        ], 8, V),
        e[7] || (e[7] = t("Group", { anchor: { Height: 10 } }, null, -1)),
        t("Group", j, [
          t("Label", {
            text: "SISTEMA DE COMBATE // PVP",
            "el-style": y.value,
            anchor: { Width: 330, Height: 38 }
          }, null, 8, J),
          t("TextButton", {
            text: "CERRAR ENLACE",
            anchor: { Width: 180, Height: 38 },
            "el-style": o(L),
            onActivating: e[2] || (e[2] = (s) => u("cerrar"))
          }, null, 40, Y)
        ])
      ]),
      _: 1
    }, 8, ["animate", "status"]));
  }
});
export {
  X as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiQmF0YWxsYVBhZ2UudnVlLmpzIiwic291cmNlcyI6W10sInNvdXJjZXNDb250ZW50IjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OzsifQ==
