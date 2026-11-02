type NavId = 'equipo' | 'crianza' | 'batalla' | 'perfil';
type __VLS_Props = {
    title?: string;
    subtitle?: string;
    active?: NavId;
    nav?: boolean;
    status?: string;
    animate?: boolean;
    fondo?: boolean;
};
declare var __VLS_115: {};
type __VLS_Slots = {} & {
    default?: (props: typeof __VLS_115) => any;
};
declare const __VLS_base: import("vue").DefineComponent<__VLS_Props, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {} & {
    nav: (id: NavId) => any;
}, string, import("vue").PublicProps, Readonly<__VLS_Props> & Readonly<{
    onNav?: ((id: NavId) => any) | undefined;
}>, {
    title: string;
    subtitle: string;
    active: NavId;
    nav: boolean;
    status: string;
    animate: boolean;
    fondo: boolean;
}, {}, {}, {}, string, import("vue").ComponentProvideOptions, false, {}, any>;
declare const __VLS_export: __VLS_WithSlots<typeof __VLS_base, __VLS_Slots>;
declare const _default: typeof __VLS_export;
export default _default;
type __VLS_WithSlots<T, S> = T & {
    new (): {
        $slots: S;
    };
};
