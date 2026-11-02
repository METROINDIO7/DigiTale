import type { DefineComponent } from 'vue'
import type { NATIVE } from 'vuetale/types/elements'

type CompanionComponent<Props> = DefineComponent<Props>

type KebabCase<Value extends string, First extends boolean = true> =
    Value extends `${infer Head}${infer Tail}`
    ? Head extends Lowercase<Head>
    ? `${Head}${KebabCase<Tail, false>}`
    : `${First extends true ? '' : '-'}${Lowercase<Head>}${KebabCase<Tail, false>}`
    : Value

type VuetaleCompanionComponents = {
    [Key in keyof NATIVE & string]: CompanionComponent<NATIVE[Key]>
} & {
    [Key in keyof NATIVE & string as KebabCase<Key>]: CompanionComponent<NATIVE[Key]>
}

declare module 'vue' {
    // eslint-disable-next-line @typescript-eslint/no-empty-object-type
    interface GlobalComponents extends VuetaleCompanionComponents { }
}

export { }