import { defineComponent as L, computed as a, openBlock as d, createBlock as N, unref as o, withCtx as W, createElementVNode as t, createElementBlock as y, Fragment as D, renderList as C } from "vue";
import { useData as u } from "vt:@core/composables/useData";
import O from "../components/CyberShell.vue.js";
import { label as r, CYBER as l, buttonStyle as c, panelBg as T, rowBg as k } from "../cyber.js";
const R = ["background"], $ = {
  "layout-mode": "Left",
  anchor: { Height: 28 }
}, M = ["el-style"], P = ["el-style"], U = ["background"], V = ["background"], q = ["text", "el-style"], w = ["asset-path", "visible"], F = {
  "layout-mode": "Top",
  anchor: { Width: 516, Height: 48 }
}, Z = ["text", "el-style"], z = ["text", "el-style"], J = ["el-style", "visible", "onActivating"], Q = ["el-style", "visible", "onActivating"], Y = ["el-style", "visible", "onActivating"], j = {
  "layout-mode": "Left",
  anchor: { Height: 38 }
}, K = ["text", "el-style"], X = ["el-style"], it = /* @__PURE__ */ L({
  __name: "SapotamaPage",
  setup(tt) {
    const m = u("titulo", "DIGIMON COMPANEROS"), h = u("slots", []), _ = u("onAction", () => {
    }), v = u("animarEntrada", !0), A = a(() => h.value.filter((i) => i.ocupado).length);
    function s(i, n) {
      const e = _.value;
      typeof e == "function" && e(JSON.stringify({ action: i, value: n }));
    }
    function b(i) {
      i !== "equipo" && s("ir_" + i);
    }
    const x = a(
      () => r(l.gold, 11, "Start", !0, { LetterSpacing: 1 })
    ), S = a(() => r(l.text, 12, "Start", !0)), f = a(() => r(l.muted, 10, "Start")), H = a(() => r(l.gold, 12, "Center", !0)), g = a(() => r(l.muted, 9, "Start", !1, { LetterSpacing: 1 })), B = c(l.cyan), E = c(l.cyan), G = c(l.magenta, "button_magenta"), I = c(l.danger, "button_danger");
    return (i, n) => (d(), N(O, {
      animate: o(v),
      title: "DIGIMON DATABASE",
      subtitle: "SAPOTAMA // EQUIPO",
      active: "equipo",
      status: "UNIDADES " + A.value + "/6",
      onNav: b
    }, {
      default: W(() => [
        t("Group", {
          "layout-mode": "Top",
          anchor: { Height: 420 },
          background: o(T),
          padding: { Full: 10 }
        }, [
          t("Group", $, [
            t("Label", {
              text: "UNIDADES ENLAZADAS",
              "el-style": x.value,
              anchor: { Width: 330 }
            }, null, 8, M),
            t("Label", {
              text: "A / B  ASIGNAR ACTIVO",
              "el-style": g.value,
              anchor: { Width: 158 }
            }, null, 8, P)
          ]),
          t("Group", {
            anchor: { Height: 2 },
            background: { Color: o(l).cyan }
          }, null, 8, U),
          n[4] || (n[4] = t("Group", { anchor: { Height: 8 } }, null, -1)),
          (d(!0), y(D, null, C(o(h), (e) => (d(), y("Group", {
            key: e.index,
            "layout-mode": "Left",
            anchor: { Height: 56 },
            background: o(k),
            padding: { Horizontal: 8, Vertical: 4 }
          }, [
            t("Label", {
              text: "0" + (e.index + 1),
              "el-style": H.value,
              anchor: { Width: 32, Height: 48 }
            }, null, 8, q),
            t("AssetImage", {
              "asset-path": e.imagen || "UI/Custom/png_Digis/Botamon.png",
              visible: e.ocupado && !!e.imagen,
              anchor: { Width: 42, Height: 42 }
            }, null, 8, w),
            n[1] || (n[1] = t("Group", { anchor: { Width: 8 } }, null, -1)),
            t("Group", F, [
              t("Label", {
                text: e.nombre,
                "el-style": S.value,
                anchor: { Height: 26 }
              }, null, 8, Z),
              t("Label", {
                text: e.info,
                "el-style": f.value,
                anchor: { Height: 20 }
              }, null, 8, z)
            ]),
            t("TextButton", {
              text: "VER",
              anchor: { Width: 80, Height: 38 },
              "el-style": o(B),
              visible: e.ocupado,
              onActivating: (p) => s("ver", e.index)
            }, null, 40, J),
            n[2] || (n[2] = t("Group", { anchor: { Width: 4 } }, null, -1)),
            t("TextButton", {
              text: "A",
              anchor: { Width: 46, Height: 38 },
              "el-style": o(E),
              visible: e.ocupado,
              onActivating: (p) => s("acta", e.index)
            }, null, 40, Q),
            n[3] || (n[3] = t("Group", { anchor: { Width: 4 } }, null, -1)),
            t("TextButton", {
              text: "B",
              anchor: { Width: 46, Height: 38 },
              "el-style": o(G),
              visible: e.ocupado,
              onActivating: (p) => s("actb", e.index)
            }, null, 40, Y)
          ], 8, V))), 128))
        ], 8, R),
        n[5] || (n[5] = t("Group", { anchor: { Height: 8 } }, null, -1)),
        t("Group", j, [
          t("Label", {
            text: "DATOS SINCRONIZADOS // " + o(m),
            "el-style": g.value,
            anchor: { Width: 330, Height: 38 }
          }, null, 8, K),
          t("TextButton", {
            text: "CERRAR ENLACE",
            anchor: { Width: 180, Height: 38 },
            "el-style": o(I),
            onActivating: n[0] || (n[0] = (e) => s("cerrar"))
          }, null, 40, X)
        ])
      ]),
      _: 1
    }, 8, ["animate", "status"]));
  }
});
export {
  it as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiU2Fwb3RhbWFQYWdlLnZ1ZS5qcyIsInNvdXJjZXMiOltdLCJzb3VyY2VzQ29udGVudCI6W10sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OyJ9
