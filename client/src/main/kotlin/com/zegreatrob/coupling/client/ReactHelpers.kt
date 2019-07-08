package com.zegreatrob.coupling.client

import react.RReadableRef

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
