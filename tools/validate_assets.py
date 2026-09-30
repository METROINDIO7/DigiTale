#!/usr/bin/env python3
"""Validador de assets DigiTale. Uso: python3 tools/validate_assets.py
Revisa: modelos->archivos, roles->modelos, beacons->roles, pngDigis, anims, loops huevo.
Salir 0 = todo OK (solo avisa de la cola larga dormida)."""
import json
import glob
import os
import sys

R = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources')
errores = []
avisos = []


def check(cond, msg, larga=False):
    if not cond:
        (avisos if larga else errores).append(msg)


real = set()
for root, _, files in os.walk(os.path.join(R, 'Common')):
    for x in files:
        real.add(os.path.relpath(os.path.join(root, x), os.path.join(R, 'Common')))

models = {}
for f in glob.glob(R + '/Server/Models/**/*.json', recursive=True):
    try:
        models[os.path.splitext(os.path.basename(f))[0]] = f
    except Exception as e:
        errores.append('Modelo ilegible: %s (%s)' % (f, e))

for mid, f in sorted(models.items()):
    try:
        d = json.load(open(f))
    except Exception as e:
        errores.append('JSON roto: %s (%s)' % (f, e))
        continue
    for k in ('Model', 'Texture'):
        v = d.get(k)
        if isinstance(v, str) and v not in real:
            errores.append('%s: %s -> %s NO EXISTE' % (mid, k, v))
    for an, spec in d.get('AnimationSets', {}).items():
        for a in spec.get('Animations', []):
            p = a.get('Animation')
            if p and p not in real:
                errores.append('%s: anim %s -> %s NO EXISTE' % (mid, an, p))

roles = {}
for f in glob.glob(R + '/Server/NPC/Roles/**/*.json', recursive=True):
    rid = os.path.splitext(os.path.basename(f))[0]
    roles[rid] = f
    try:
        json.load(open(f))
    except Exception as e:
        errores.append('Rol JSON roto: %s (%s)' % (rid, e))

for rid, f in sorted(roles.items()):
    if 'Template' in rid:
        continue
    try:
        d = json.load(open(f))
    except Exception:
        continue
    m = d.get('Modify', {})
    app = m.get('Appearance') if isinstance(m, dict) else d.get('Appearance')
    if app and app not in models:
        avisos.append('%s -> modelo %s NO EXISTE (dormido hasta modelarlo)' % (rid, app))

for f in glob.glob(R + '/Server/NPC/Spawn/**/*.json', recursive=True):
    try:
        d = json.load(open(f))
    except Exception as e:
        errores.append('Spawn JSON roto: %s (%s)' % (f, e))
        continue
    for n in d.get('NPCs', []):
        if not isinstance(n, dict):
            continue
        rid = n.get('Id') or n.get('Name')
        if rid not in roles:
            errores.append('%s: rol %s NO EXISTE' % (os.path.basename(f), rid))

# pngDigis vs DigiImagenes.java
try:
    java = open('src/main/java/com/digitale/ui/DigiImagenes.java').read()
    import re
    usados = set(re.findall(r'"UI/Custom/png_Digis/([^"]+)"', java))
    for u in sorted(usados):
        check('UI/Custom/png_Digis/' + u in real or 'Custom/UI/Custom/png_Digis/' + u in real,
              'DigiImagenes usa %s NO EXISTE' % u)
except FileNotFoundError:
    pass

# Loops huevo item -> npc -> item
def buscar_drops(o, acc):
    if isinstance(o, dict):
        if o.get('Type') == 'DropItem' and 'Item' in o:
            acc.append(o['Item'])
        for v in o.values():
            buscar_drops(v, acc)
    elif isinstance(o, list):
        for x in o:
            buscar_drops(x, acc)


for f in sorted(glob.glob(R + '/Server/Item/Items/Eggs/Digitama_*.json')):
    item = os.path.splitext(os.path.basename(f))[0]
    try:
        d = json.load(open(f))
        ent = d['Interactions']['Primary']['Interactions'][0].get('EntityId')
        rf = R + '/Server/NPC/Roles/Digitama/%s.json' % ent
        if not os.path.exists(rf):
            errores.append('%s: npc %s SIN ROL' % (item, ent))
            continue
        rd = json.load(open(rf))
        drops = []
        buscar_drops(rd, drops)
        check(drops == [item], '%s: loop roto (npc %s suelta %s)' % (item, ent, drops))
    except Exception as e:
        errores.append('%s: %s' % (item, e))

print('ERRORES (%d):' % len(errores))
for e in errores:
    print('  X', e)
print('AVISOS cola larga (%d, ver solo conteo)' % len(avisos))
sys.exit(1 if errores else 0)
