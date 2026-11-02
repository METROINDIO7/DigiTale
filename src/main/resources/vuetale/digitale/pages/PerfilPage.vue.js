import { defineComponent as Y, computed as c, openBlock as s, createBlock as J, unref as e, withCtx as K, createElementVNode as t, createElementBlock as i, Fragment as h, renderList as f, createCommentVNode as j } from "vue";
import { useData as g } from "vt:@core/composables/useData";
import X from "../components/CyberShell.vue.js";
import { label as _, CYBER as a, buttonStyle as m, panelBg as tt, captionStyle as V, rowBg as O } from "../cyber.js";
const et = {
  "layout-mode": "Left",
  anchor: { Height: 34 }
}, lt = ["text", "el-style", "onActivating"], ot = {
  key: 0,
  anchor: { Width: 6 }
}, nt = ["background"], st = {
  "layout-mode": "Left",
  anchor: { Height: 124 }
}, it = ["asset-path", "visible"], at = {
  "layout-mode": "Top",
  anchor: { Width: 640, Height: 118 }
}, ut = ["text", "el-style"], rt = ["text", "el-style"], ht = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, dt = ["el-style"], ct = ["asset-path", "visible"], yt = ["text", "el-style"], gt = ["el-style"], _t = ["asset-path", "visible"], pt = ["text", "el-style"], vt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, bt = ["el-style"], xt = ["text", "el-style"], mt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, Lt = ["el-style"], Ht = ["text", "el-style"], Gt = ["background"], ft = {
  "layout-mode": "Left",
  anchor: { Height: 82 }
}, Wt = {
  "layout-mode": "Top",
  anchor: { Width: 238, Height: 82 }
}, St = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, At = ["el-style"], Et = ["text", "el-style"], kt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, It = ["el-style"], Ot = ["text", "el-style"], Tt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, Ct = ["el-style"], Nt = ["text", "el-style"], Dt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, Bt = ["el-style"], Rt = ["text", "el-style"], Vt = {
  "layout-mode": "Top",
  anchor: { Width: 238, Height: 82 }
}, Ut = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, Pt = ["el-style"], Mt = ["text", "el-style"], qt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, $t = ["el-style"], Ft = ["text", "el-style"], wt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, zt = ["el-style"], Qt = ["text", "el-style"], Zt = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, Yt = ["el-style"], Jt = ["text", "el-style"], Kt = ["background"], jt = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, Xt = ["el-style"], te = ["text", "el-style"], ee = ["el-style"], le = ["text", "el-style"], oe = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, ne = ["el-style"], se = ["el-style"], ie = ["el-style"], ae = ["text", "el-style"], ue = {
  "layout-mode": "Left",
  anchor: { Height: 34 }
}, re = ["el-style"], he = ["text", "el-style"], de = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, ce = ["el-style"], ye = ["el-style"], ge = ["background"], _e = ["text", "el-style", "onActivating"], pe = ["background"], ve = ["text", "el-style"], be = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, xe = ["el-style"], me = ["text", "el-style"], Le = ["el-style"], He = ["text", "el-style"], Ge = {
  "layout-mode": "Left",
  anchor: { Height: 20 }
}, fe = ["el-style"], We = ["text", "el-style"], Se = ["el-style"], Ae = ["text", "el-style"], Ee = ["text", "el-style"], ke = ["el-style"], Ie = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, Oe = ["el-style"], Te = ["text", "el-style"], Ce = ["background"], Ne = ["text", "el-style", "onActivating"], De = ["text", "el-style"], Be = ["el-style"], Re = ["background"], Ve = ["el-style"], Ue = ["text", "el-style"], Pe = ["text", "el-style"], Me = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, qe = ["el-style"], $e = ["text", "el-style"], Fe = ["el-style"], we = ["text", "el-style", "onActivating"], ze = ["el-style"], Qe = ["background"], Ze = ["el-style"], Ye = ["text", "el-style"], Je = {
  "layout-mode": "Left",
  anchor: { Height: 22 }
}, Ke = ["el-style"], je = ["el-style"], Xe = ["background"], tl = ["text", "el-style"], el = ["text", "el-style", "onActivating"], ll = ["background"], ol = ["el-style"], nl = {
  "layout-mode": "Left",
  anchor: { Height: 38 }
}, sl = ["text", "el-style"], il = ["text", "el-style"], al = {
  "layout-mode": "Left",
  anchor: { Height: 30 }
}, ul = ["text", "el-style"], rl = ["el-style"], pl = /* @__PURE__ */ Y({
  __name: "PerfilPage",
  setup(hl) {
    const T = g("titulo", "PERFIL DEL DIGIMON"), b = g("pestana", "stats"), n = g("stats", {
      vacio: !0,
      nombre: "-",
      especie: "-",
      nivel: "-",
      elemento: "-",
      atributo: "-",
      hp: "- / -",
      atk: "-",
      def: "-",
      spd: "-",
      wis: "-",
      lazo: "- / 100",
      disciplina: "-",
      victorias: "-",
      derrotas: "-",
      energia: "- / 100",
      hambre: "- / 100",
      fuerte: "-",
      debil: "-",
      imagen: "",
      iconoElemento: null,
      iconoAtributo: null
    }), C = g("movimientos", []), E = g("movimientoSeleccionado", 0), W = g("evolucion", {
      actual: "-",
      condiciones: "",
      info: "",
      opciones: []
    }), A = g("deEvolucion", {
      actual: "-",
      info: "",
      opciones: []
    }), S = g("equipamiento", {
      equipo: "Sin equipo",
      arma: "Sin arma",
      armadura: "Sin armadura",
      cosmetico1: "Aspecto Normal",
      cosmetico2: "Aspecto Especial"
    }), U = g("onAction", () => {
    }), P = g("animarEntrada", !0), N = [
      { id: "stats", label: "STATS" },
      { id: "movimientos", label: "MOVIM." },
      { id: "evolucion", label: "EVOL." },
      { id: "de_evolucion", label: "DEEVO." },
      { id: "equipamiento", label: "EQUIPO" }
    ], M = c(() => {
      const y = [], l = C.value;
      for (let o = 0; o < l.length; o += 2)
        y.push(l.slice(o, o + 2));
      return y;
    }), x = c(
      () => C.value.find((y) => y.index === E.value)
    );
    function d(y, l) {
      const o = U.value;
      typeof o == "function" && o(JSON.stringify({ action: y, value: l }));
    }
    function q(y) {
      y === "equipo" ? d("volver") : y === "crianza" ? d("ir_crianza") : y === "batalla" && d("ir_batalla");
    }
    const p = c(
      () => _(a.gold, 11, "Start", !0, { LetterSpacing: 1 })
    ), v = c(() => _(a.muted, 10, "Start", !1, { Wrap: !0 })), u = c(() => _(a.cyan, 10, "Start", !0, { LetterSpacing: 1 })), r = c(() => _(a.text, 11, "Start")), $ = c(() => _(a.text, 14, "Start", !0)), k = c(() => _(a.green, 11, "Start")), F = c(() => _(a.magenta, 11, "Start")), I = c(() => _(a.gold, 11, "Start")), D = c(() => _("#ff8a5c", 11, "Start")), B = c(() => _(a.muted, 9, "Start", !1, { LetterSpacing: 1 })), w = m(a.muted), z = m(a.gold, "button_active"), L = m(a.cyan), Q = m(a.gold, "button_active"), R = m(a.muted, "panel_soft"), Z = m(a.cyan);
    return (y, l) => (s(), J(X, {
      animate: e(P),
      title: "DIGIMON ANALYZER",
      subtitle: "PERFIL // " + e(n).nombre,
      active: "perfil",
      status: "NIVEL " + e(n).nivel,
      onNav: q
    }, {
      default: K(() => [
        t("Group", et, [
          (s(), i(h, null, f(N, (o, H) => (s(), i(h, {
            key: o.id + (e(b) === o.id ? "-on" : "-off")
          }, [
            t("TextButton", {
              text: (e(b) === o.id ? "> " : "") + o.label,
              anchor: { Width: 140, Height: 34 },
              "el-style": e(b) === o.id ? e(z) : e(w),
              onActivating: (G) => d("tab", o.id)
            }, null, 40, lt),
            H < N.length - 1 ? (s(), i("Group", ot)) : j("", !0)
          ], 64))), 64))
        ]),
        l[32] || (l[32] = t("Group", { anchor: { Height: 6 } }, null, -1)),
        t("Group", {
          "layout-mode": "Top",
          anchor: { Height: 400 },
          background: e(tt),
          padding: { Full: 10 }
        }, [
          e(b) === "stats" ? (s(), i(h, { key: 0 }, [
            t("Group", st, [
              t("AssetImage", {
                "asset-path": e(n).imagen || "UI/Custom/png_Digis/Botamon.png",
                visible: !e(n).vacio && !!e(n).imagen,
                anchor: { Width: 112, Height: 112 }
              }, null, 8, it),
              l[8] || (l[8] = t("Group", { anchor: { Width: 10 } }, null, -1)),
              t("Group", at, [
                t("Label", {
                  text: e(n).nombre,
                  "el-style": $.value,
                  anchor: { Height: 26 }
                }, null, 8, ut),
                t("Label", {
                  text: e(n).especie + "  //  " + e(n).nivel,
                  "el-style": v.value,
                  anchor: { Height: 18 }
                }, null, 8, rt),
                l[7] || (l[7] = t("Group", { anchor: { Height: 4 } }, null, -1)),
                t("Group", ht, [
                  t("Label", {
                    text: "ELEM",
                    "el-style": u.value,
                    anchor: { Width: 46 }
                  }, null, 8, dt),
                  t("AssetImage", {
                    "asset-path": e(n).iconoElemento || "UI/Textures/Icons/Elemental/Neutral.png",
                    visible: !!e(n).iconoElemento,
                    anchor: { Width: 18, Height: 18 }
                  }, null, 8, ct),
                  l[4] || (l[4] = t("Group", { anchor: { Width: 4 } }, null, -1)),
                  t("Label", {
                    text: e(n).elemento,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, yt),
                  l[5] || (l[5] = t("Group", { anchor: { Width: 18 } }, null, -1)),
                  t("Label", {
                    text: "TIPO",
                    "el-style": u.value,
                    anchor: { Width: 40 }
                  }, null, 8, gt),
                  t("AssetImage", {
                    "asset-path": e(n).iconoAtributo || "UI/Textures/Icons/Type/None.png",
                    visible: !!e(n).iconoAtributo,
                    anchor: { Width: 18, Height: 18 }
                  }, null, 8, _t),
                  l[6] || (l[6] = t("Group", { anchor: { Width: 4 } }, null, -1)),
                  t("Label", {
                    text: e(n).atributo,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, pt)
                ]),
                t("Group", vt, [
                  t("Label", {
                    text: "HP",
                    "el-style": u.value,
                    anchor: { Width: 46 }
                  }, null, 8, bt),
                  t("Label", {
                    text: e(n).hp,
                    "el-style": k.value,
                    anchor: { Height: 20 }
                  }, null, 8, xt)
                ]),
                t("Group", mt, [
                  t("Label", {
                    text: "LAZO",
                    "el-style": u.value,
                    anchor: { Width: 46 }
                  }, null, 8, Lt),
                  t("Label", {
                    text: e(n).lazo,
                    "el-style": F.value,
                    anchor: { Height: 20 }
                  }, null, 8, Ht)
                ])
              ])
            ]),
            l[11] || (l[11] = t("Group", { anchor: { Height: 4 } }, null, -1)),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: e(a).cyan }
            }, null, 8, Gt),
            l[12] || (l[12] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Group", ft, [
              t("Group", Wt, [
                t("Group", St, [
                  t("Label", {
                    text: "ATK",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, At),
                  t("Label", {
                    text: e(n).atk,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Et)
                ]),
                t("Group", kt, [
                  t("Label", {
                    text: "DEF",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, It),
                  t("Label", {
                    text: e(n).def,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Ot)
                ]),
                t("Group", Tt, [
                  t("Label", {
                    text: "SPD",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, Ct),
                  t("Label", {
                    text: e(n).spd,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Nt)
                ]),
                t("Group", Dt, [
                  t("Label", {
                    text: "WIS",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, Bt),
                  t("Label", {
                    text: e(n).wis,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Rt)
                ])
              ]),
              l[9] || (l[9] = t("Group", { anchor: { Width: 12 } }, null, -1)),
              t("Group", Vt, [
                t("Group", Ut, [
                  t("Label", {
                    text: "DISC.",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, Pt),
                  t("Label", {
                    text: e(n).disciplina,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Mt)
                ]),
                t("Group", qt, [
                  t("Label", {
                    text: "VICT.",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, $t),
                  t("Label", {
                    text: e(n).victorias,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Ft)
                ]),
                t("Group", wt, [
                  t("Label", {
                    text: "DERR.",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, zt),
                  t("Label", {
                    text: e(n).derrotas,
                    "el-style": r.value,
                    anchor: { Height: 20 }
                  }, null, 8, Qt)
                ]),
                t("Group", Zt, [
                  t("Label", {
                    text: "ENERGIA",
                    "el-style": u.value,
                    anchor: { Width: 70 }
                  }, null, 8, Yt),
                  t("Label", {
                    text: e(n).energia,
                    "el-style": I.value,
                    anchor: { Height: 20 }
                  }, null, 8, Jt)
                ])
              ])
            ]),
            l[13] || (l[13] = t("Group", { anchor: { Height: 4 } }, null, -1)),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: e(a).magenta }
            }, null, 8, Kt),
            l[14] || (l[14] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Group", jt, [
              t("Label", {
                text: "HAMBRE",
                "el-style": u.value,
                anchor: { Width: 90 }
              }, null, 8, Xt),
              t("Label", {
                text: e(n).hambre,
                "el-style": D.value,
                anchor: { Width: 150 }
              }, null, 8, te),
              t("Label", {
                text: "FUERTE VS",
                "el-style": k.value,
                anchor: { Width: 90 }
              }, null, 8, ee),
              t("Label", {
                text: e(n).fuerte,
                "el-style": r.value,
                anchor: { Width: 150 }
              }, null, 8, le)
            ]),
            t("Group", oe, [
              t("Label", {
                text: "ESTADO",
                "el-style": u.value,
                anchor: { Width: 90 }
              }, null, 8, ne),
              t("Label", {
                text: "OPERATIVO",
                "el-style": k.value,
                anchor: { Width: 150 }
              }, null, 8, se),
              t("Label", {
                text: "DEBIL VS",
                "el-style": D.value,
                anchor: { Width: 90 }
              }, null, 8, ie),
              t("Label", {
                text: e(n).debil,
                "el-style": r.value,
                anchor: { Width: 150 }
              }, null, 8, ae)
            ]),
            l[15] || (l[15] = t("Group", { anchor: { Height: 10 } }, null, -1)),
            t("Group", ue, [
              t("TextButton", {
                text: "VOLVER AL EQUIPO",
                anchor: { Width: 200, Height: 34 },
                "el-style": e(Z),
                onActivating: l[0] || (l[0] = (o) => d("volver"))
              }, null, 40, re),
              l[10] || (l[10] = t("Group", { anchor: { Width: 8 } }, null, -1)),
              t("Label", {
                text: "ANALISIS // " + e(T),
                "el-style": e(V)(e(a).muted),
                anchor: { Width: 360, Height: 34 }
              }, null, 8, he)
            ])
          ], 64)) : e(b) === "movimientos" ? (s(), i(h, { key: 1 }, [
            t("Group", de, [
              t("Label", {
                text: "MOVIMIENTOS / TECNICAS",
                "el-style": p.value,
                anchor: { Width: 360 }
              }, null, 8, ce),
              t("Label", {
                text: "SELECCIONA UNO",
                "el-style": B.value,
                anchor: { Width: 128 }
              }, null, 8, ye)
            ]),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: e(a).cyan }
            }, null, 8, ge),
            l[18] || (l[18] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            (s(!0), i(h, null, f(M.value, (o, H) => (s(), i("Group", {
              key: H,
              "layout-mode": "Left",
              anchor: { Height: 44 }
            }, [
              (s(!0), i(h, null, f(o, (G) => (s(), i("TextButton", {
                key: G.index + (e(E) === G.index ? "-on" : "-off"),
                text: G.nombre,
                anchor: { Width: 380, Height: 36 },
                "el-style": e(E) === G.index ? e(Q) : e(L),
                onActivating: (dl) => d("seleccionar_movimiento", G.index)
              }, null, 40, _e))), 128)),
              l[16] || (l[16] = t("Group", { anchor: { Width: 8 } }, null, -1))
            ]))), 128)),
            l[19] || (l[19] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            x.value ? (s(), i("Group", {
              key: 0,
              "layout-mode": "Top",
              anchor: { Height: 172 },
              background: e(O),
              padding: { Full: 8 }
            }, [
              t("Label", {
                text: "SELECCIONADO // " + x.value.nombre,
                "el-style": p.value,
                anchor: { Height: 18 }
              }, null, 8, ve),
              t("Group", be, [
                t("Label", {
                  text: "PODER",
                  "el-style": u.value,
                  anchor: { Width: 80 }
                }, null, 8, xe),
                t("Label", {
                  text: String(x.value.poder),
                  "el-style": r.value,
                  anchor: { Width: 120 }
                }, null, 8, me),
                t("Label", {
                  text: "GOLPES",
                  "el-style": u.value,
                  anchor: { Width: 80 }
                }, null, 8, Le),
                t("Label", {
                  text: String(x.value.golpes),
                  "el-style": r.value,
                  anchor: { Width: 120 }
                }, null, 8, He)
              ]),
              t("Group", Ge, [
                t("Label", {
                  text: "CRITICO",
                  "el-style": u.value,
                  anchor: { Width: 80 }
                }, null, 8, fe),
                t("Label", {
                  text: String(x.value.critico),
                  "el-style": r.value,
                  anchor: { Width: 120 }
                }, null, 8, We),
                t("Label", {
                  text: "ALCANCE",
                  "el-style": u.value,
                  anchor: { Width: 80 }
                }, null, 8, Se),
                t("Label", {
                  text: x.value.alcance,
                  "el-style": r.value,
                  anchor: { Width: 120 }
                }, null, 8, Ae)
              ]),
              l[17] || (l[17] = t("Group", { anchor: { Height: 4 } }, null, -1)),
              t("Label", {
                text: x.value.descripcion,
                "el-style": v.value,
                anchor: { Height: 56 }
              }, null, 8, Ee)
            ], 8, pe)) : (s(), i("Label", {
              key: 1,
              text: "Selecciona un movimiento para ver detalles.",
              "el-style": v.value,
              anchor: { Height: 24 }
            }, null, 8, ke))
          ], 64)) : e(b) === "evolucion" ? (s(), i(h, { key: 2 }, [
            t("Group", Ie, [
              t("Label", {
                text: "EVOLUCION",
                "el-style": p.value,
                anchor: { Width: 360 }
              }, null, 8, Oe),
              t("Label", {
                text: e(W).actual,
                "el-style": r.value,
                anchor: { Width: 128 }
              }, null, 8, Te)
            ]),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: e(a).magenta }
            }, null, 8, Ce),
            l[22] || (l[22] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            e(W).opciones.length > 0 ? (s(!0), i(h, { key: 0 }, f(e(W).opciones, (o) => (s(), i("Group", {
              key: o.destino,
              "layout-mode": "Left",
              anchor: { Height: 44 }
            }, [
              (s(), i("TextButton", {
                key: o.destino + (o.disponible ? "-on" : "-off"),
                text: o.destino + (o.etapa ? "  [" + o.etapa + "]" : ""),
                anchor: { Width: 380, Height: 36 },
                "el-style": o.disponible ? e(L) : e(R),
                onActivating: (H) => d("evolucionar", o.destino)
              }, null, 40, Ne)),
              l[20] || (l[20] = t("Group", { anchor: { Width: 8 } }, null, -1)),
              t("Label", {
                text: o.condiciones,
                "el-style": v.value,
                anchor: { Width: 380, Height: 36 }
              }, null, 8, De)
            ]))), 128)) : (s(), i("Label", {
              key: 1,
              text: "Evolucion no disponible.",
              "el-style": v.value,
              anchor: { Height: 24 }
            }, null, 8, Be)),
            l[23] || (l[23] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            t("Group", {
              "layout-mode": "Top",
              anchor: { Height: 176 },
              background: e(O),
              padding: { Full: 8 }
            }, [
              t("Label", {
                text: "CONDICIONES DE EVOLUCION",
                "el-style": p.value,
                anchor: { Height: 18 }
              }, null, 8, Ve),
              t("Label", {
                text: e(W).condiciones,
                "el-style": v.value,
                anchor: { Height: 60 }
              }, null, 8, Ue),
              l[21] || (l[21] = t("Group", { anchor: { Height: 4 } }, null, -1)),
              t("Label", {
                text: e(W).info,
                "el-style": I.value,
                anchor: { Height: 52 }
              }, null, 8, Pe)
            ], 8, Re)
          ], 64)) : e(b) === "de_evolucion" ? (s(), i(h, { key: 3 }, [
            t("Group", Me, [
              t("Label", {
                text: "DE-EVOLUCION",
                "el-style": p.value,
                anchor: { Width: 360 }
              }, null, 8, qe),
              t("Label", {
                text: e(A).actual,
                "el-style": r.value,
                anchor: { Width: 128 }
              }, null, 8, $e)
            ]),
            l[25] || (l[25] = t("Group", {
              anchor: { Height: 2 },
              background: { Color: "#ff8a5c" }
            }, null, -1)),
            l[26] || (l[26] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Label", {
              text: "La de-evolucion devuelve a tu Digimon a una forma anterior.",
              "el-style": v.value,
              anchor: { Height: 24 }
            }, null, 8, Fe),
            e(A).opciones.length > 0 ? (s(), i(h, { key: 0 }, [
              (s(!0), i(h, null, f(e(A).opciones, (o) => (s(), i("TextButton", {
                key: o.destino + (o.disponible ? "-on" : "-off"),
                text: o.destino,
                anchor: { Width: 380, Height: 36 },
                "el-style": o.disponible ? e(L) : e(R),
                onActivating: (H) => d("de_evolucionar", o.destino)
              }, null, 40, we))), 128)),
              l[24] || (l[24] = t("Group", { anchor: { Height: 6 } }, null, -1))
            ], 64)) : (s(), i("Label", {
              key: 1,
              text: "No se puede de-evolucionar mas.",
              "el-style": v.value,
              anchor: { Height: 24 }
            }, null, 8, ze)),
            l[27] || (l[27] = t("Group", { anchor: { Height: 6 } }, null, -1)),
            t("Group", {
              "layout-mode": "Top",
              anchor: { Height: 108 },
              background: e(O),
              padding: { Full: 8 }
            }, [
              t("Label", {
                text: "RESULTADO",
                "el-style": p.value,
                anchor: { Height: 18 }
              }, null, 8, Ze),
              t("Label", {
                text: e(A).info,
                "el-style": I.value,
                anchor: { Height: 56 }
              }, null, 8, Ye)
            ], 8, Qe)
          ], 64)) : (s(), i(h, { key: 4 }, [
            t("Group", Je, [
              t("Label", {
                text: "EQUIPAMIENTO",
                "el-style": p.value,
                anchor: { Width: 360 }
              }, null, 8, Ke),
              t("Label", {
                text: "SLOTS DE DATOS",
                "el-style": B.value,
                anchor: { Width: 128 }
              }, null, 8, je)
            ]),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: e(a).cyan }
            }, null, 8, Xe),
            l[29] || (l[29] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            (s(!0), i(h, null, f([
              { label: "EQUIPO ACTUAL", key: "equipo", value: e(S).equipo },
              { label: "ARMA", key: "arma", value: e(S).arma },
              { label: "ARMADURA", key: "armadura", value: e(S).armadura }
            ], (o) => (s(), i("Group", {
              key: o.key,
              "layout-mode": "Left",
              anchor: { Height: 44 }
            }, [
              t("Label", {
                text: o.label,
                "el-style": u.value,
                anchor: { Width: 140, Height: 42 }
              }, null, 8, tl),
              t("TextButton", {
                text: o.value,
                anchor: { Width: 600, Height: 38 },
                "el-style": e(L),
                onActivating: (H) => d("equipamiento", o.key)
              }, null, 40, el)
            ]))), 128)),
            l[30] || (l[30] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            t("Group", {
              anchor: { Height: 2 },
              background: { Color: e(a).magenta }
            }, null, 8, ll),
            l[31] || (l[31] = t("Group", { anchor: { Height: 8 } }, null, -1)),
            t("Label", {
              text: "COSMETICOS",
              "el-style": p.value,
              anchor: { Height: 18 }
            }, null, 8, ol),
            t("Group", nl, [
              t("TextButton", {
                text: e(S).cosmetico1,
                anchor: { Width: 380, Height: 38 },
                "el-style": e(L),
                onActivating: l[1] || (l[1] = (o) => d("equipamiento", "cosmetico1"))
              }, null, 40, sl),
              l[28] || (l[28] = t("Group", { anchor: { Width: 8 } }, null, -1)),
              t("TextButton", {
                text: e(S).cosmetico2,
                anchor: { Width: 380, Height: 38 },
                "el-style": e(L),
                onActivating: l[2] || (l[2] = (o) => d("equipamiento", "cosmetico2"))
              }, null, 40, il)
            ])
          ], 64))
        ], 8, nt),
        l[33] || (l[33] = t("Group", { anchor: { Height: 8 } }, null, -1)),
        t("Group", al, [
          t("Label", {
            text: "ANALIZADOR DIGITAL // " + e(T),
            "el-style": e(V)(e(a).muted),
            anchor: { Width: 330, Height: 30 }
          }, null, 8, ul),
          t("TextButton", {
            text: "CERRAR",
            anchor: { Width: 180, Height: 30 },
            "el-style": e(m)(e(a).danger, "button_danger"),
            onActivating: l[3] || (l[3] = (o) => d("cerrar"))
          }, null, 40, rl)
        ])
      ]),
      _: 1
    }, 8, ["animate", "subtitle", "status"]));
  }
});
export {
  pl as default
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiUGVyZmlsUGFnZS52dWUuanMiLCJzb3VyY2VzIjpbXSwic291cmNlc0NvbnRlbnQiOltdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7In0=
