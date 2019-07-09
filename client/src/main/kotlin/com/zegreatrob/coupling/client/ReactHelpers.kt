package com.zegreatrob.coupling.client

import react.*

@JsModule("react")
@JsNonModule
private external val React: dynamic

fun <T> useRef(default: T?) = React.useRef(default).unsafeCast<RReadableRef<T>>()

fun useLayoutEffect(callback: () -> Unit) {
    React.useLayoutEffect {
        callback()
        undefined
    }
}

fun <T : RProps> rFunction(handler: RBuilder.(props: T) -> Unit) = { props: T ->
    buildElement {
        handler(props)
    }
}.unsafeCast<RClass<T>>()

fun <P : RProps> RBuilder.element(clazz: RClass<P>, props: P, handler: RHandler<P> = {}) {
    child(
            type = clazz,
            props = props,
            handler = handler
    )
}