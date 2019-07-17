package com.zegreatrob.coupling.client

import react.*
import kotlin.reflect.KFunction1

@JsModule("react")
@JsNonModule
private external val React: dynamic

@JsModule("core-js/features/object/assign")
@JsNonModule
external fun <T, R : T> objectAssign(dest: R, vararg src: T): R

fun <T> useRef(default: T?) = React.useRef(default).unsafeCast<RReadableRef<T>>()

fun useLayoutEffect(callback: () -> Unit) {
    React.useLayoutEffect {
        callback()
        undefined
    }
}

fun <T> useState(default: T): StateValueContent<T> {
    val stateArray = React.useState(default)
    return StateValueContent(
            value = stateArray[0].unsafeCast<T>(),
            setter = stateArray[1].unsafeCast<(T) -> Unit>()
    )
}

interface RFunction<P : RProps> : RClass<P>

data class StateValueContent<T>(val value: T, val setter: (T) -> Unit)

inline fun <reified T : RProps> rFunction(crossinline handler: RBuilder.(props: T) -> ReactElement) = { props: T ->
    buildElement {
        handler(restoreKotlinType(props))
    }
}.unsafeCast<RFunction<T>>()

inline fun <reified T : RProps> restoreKotlinType(@Suppress("UNUSED_PARAMETER") props: T): T {
    @Suppress("UNUSED_VARIABLE") val jsClass = T::class.js.unsafeCast<T>()
    return if (props::class.js == jsClass) {
        props
    } else {
        val newProps = js("new jsClass()")
        objectAssign(newProps, props)
        newProps.unsafeCast<T>()
    }
}

inline fun <reified P : RProps> RBuilder.element(clazz: RClass<P>, props: P, key: String? = null, noinline handler: RHandler<P> = {}) {
    key?.let { props.key = it }
    child(
            type = clazz,
            props = props,
            handler = handler
    )
}

inline fun <reified P : RProps> RBuilder.element(func: KFunction1<P, ReactElement?>, props: P, key: String? = null, noinline handler: RHandler<P> = {}) =
        element(func.rFunction(), props, key, handler)

inline fun <reified T : RProps> KFunction1<T, ReactElement?>.rFunction() =
        { it: T -> this(restoreKotlinType(it)) }.unsafeCast<RClass<T>>()