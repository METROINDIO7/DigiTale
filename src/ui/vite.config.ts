import { fileURLToPath, URL } from 'node:url'

import { defineConfig, type UserConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import tailwindcss from '@tailwindcss/vite'
import vueDevTools from 'vite-plugin-vue-devtools'
import { resolve } from 'node:path'
import { readdirSync } from 'node:fs'
import { existsSync, readFileSync } from 'node:fs'
import { AssetSyncPlugin, CssBuildPlugin, HmrIdsPlugin, VuetalePlugin } from 'vuetale/vite'
import { NATIVE_UI_TAGS } from 'vuetale'
import vuetaleConfig from './.vuetale/config.json' with { type: 'json' }
import vuetalePlugin from './lib/vuetale-plugin.json' with { type: 'json' }

function loadVuetaleAliases(): Record<string, string> {
  const aliasesFile = resolve(__dirname, '.vuetale/aliases.json')
  if (!existsSync(aliasesFile)) return {}
  try {
    return JSON.parse(readFileSync(aliasesFile, 'utf-8'))
  } catch {
    return {}
  }
}

const vuetaleAliases = loadVuetaleAliases()

function getComponentEntries() {
  const componentsDir = resolve(__dirname, 'lib/components')

  if (!existsSync(componentsDir)) return {}

  return readdirSync(componentsDir)
    .filter(file => file.endsWith('.vue'))
    .reduce((entries, file) => {
      const name = file.replace('.vue', '')
      entries[name] = resolve(componentsDir, file)
      return entries
    }, {} as Record<string, string>)
}

function getScriptEntries(mode: string) {
  const scriptsDir = resolve(__dirname, 'lib')

  if (!existsSync(scriptsDir)) return {}

  return readdirSync(scriptsDir)
    .filter(file => file.endsWith('.ts'))
    // debug.ts is only useful in dev mode – skip it in production builds
    .filter(file => mode === 'development' || file !== 'debug.ts')
    .reduce((entries, file) => {
      const name = file.replace('.ts', '')
      entries[name] = resolve(scriptsDir, file)
      return entries
    }, {} as Record<string, string>)
}


function getPageEntries() {
  const pagesDir = resolve(__dirname, 'lib/pages')
  return readdirSync(pagesDir)
    .filter(file => file.endsWith('.vue'))
    .reduce((entries, file) => {
      const name = file.replace('.vue', '')
      entries[name] = resolve(pagesDir, file)
      return entries
    }, {} as Record<string, string>)
}

function getHudEntries() {
  const hudsDir = resolve(__dirname, 'lib/huds')

  if (!existsSync(hudsDir)) return {}
  return readdirSync(hudsDir)
    .filter(file => file.endsWith('.vue'))
    .reduce((entries, file) => {
      const name = file.replace('.vue', '')
      entries[name] = resolve(hudsDir, file)
      return entries
    }, {} as Record<string, string>)
}

function getComposableEntries() {
  const composablesDir = resolve(__dirname, 'lib/composables')

  if (!existsSync(composablesDir)) return {}

  return readdirSync(composablesDir)
    .filter(file => file.endsWith('.ts'))
    .reduce((entries, file) => {
      const name = file.replace('.ts', '')
      entries[`composables/${name}`] = resolve(composablesDir, file)
      return entries
    }, {} as Record<string, string>)
}


// https://vite.dev/config/
export default defineConfig(({ mode }): UserConfig => ({
  plugins: [
    tailwindcss(),
    vue({
      template: {
        compilerOptions: {
          // Treat custom renderer tags as native elements instead of Vue components.
          isCustomElement: (tag) => NATIVE_UI_TAGS.has(tag),
        },
      },
    }),
    vueJsx(),

    vueDevTools(),
    VuetalePlugin(),
    CssBuildPlugin(),
    HmrIdsPlugin(),
    AssetSyncPlugin(__dirname, vuetaleConfig.resourcesLocation, vuetalePlugin.name),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./lib', import.meta.url)),
      ...vuetaleAliases,
    },
  },
  build: {
    cssCodeSplit: true,
    sourcemap: 'inline',
    watch: mode === 'development'
      ? {
        include: ['lib/**'],
        exclude: ['**/assets.ts'],
      }
      : null,
    lib: {
      entry: {
        // you can still have a main barrel if you want
        // index: resolve(__dirname, 'src/index.ts'),
        ...getPageEntries(),   // Button, Card, Modal, ...
        ...getHudEntries(),
        ...getComponentEntries(),
        ...getScriptEntries(mode),
        ...getComposableEntries()
      },
      formats: ['es'],
      fileName: (_, entryName) => `${entryName}.js`
    },


    rollupOptions: {
      external: (id) => {
        if (id === 'vue') return true
        if (id.startsWith('vt:')) return true
      },
      output: {
        preserveModules: true,
        entryFileNames: '[name].js',

      },
    },

    outDir: resolve(vuetaleConfig.resourcesLocation, 'vuetale', vuetalePlugin.name),
    //outDir: '../main/resources/vuetale/' + vuetaleConfig.name,
    // Keep declarations produced by watch:types; clean only on production builds.
    emptyOutDir: mode !== 'development'
  }
}))
