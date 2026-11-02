/**
 * Resolves the same outDir used by vite.config.ts and runs vue-tsc
 * with --declaration --emitDeclarationOnly so that .d.ts files land
 * alongside the compiled JS in the resources directory.
 *
 * Usage:
 *   tsx scripts/build-types.ts [--watch]
 */
import { resolve } from 'node:path'
import { readFileSync } from 'node:fs'
import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const __dirname = fileURLToPath(new URL('.', import.meta.url))
const root = resolve(__dirname, '..')

const vuetaleConfig = JSON.parse(readFileSync(resolve(root, '.vuetale/config.json'), 'utf-8'))
const vuetalePlugin = JSON.parse(readFileSync(resolve(root, 'lib/vuetale-plugin.json'), 'utf-8'))

const outDir = resolve(vuetaleConfig.resourcesLocation, 'vuetale', vuetalePlugin.name)

const watchMode = process.argv.includes('--watch')

const args = [
    'vue-tsc',
    '--declaration',
    '--emitDeclarationOnly',
    '--outDir', outDir,
    '-p', 'tsconfig.build-types.json',
    ...(watchMode ? ['--watch'] : []),
]

console.log(`[build-types] outDir → ${outDir}`)

const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx'

const result = spawnSync(npx, ['--no-install', ...args], {
    cwd: root,
    stdio: 'inherit',
})

process.exit(result.status ?? 0)
