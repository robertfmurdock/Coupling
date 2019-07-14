package com.zegreatrob.coupling.client

import react.*

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

data class StateValueContent<T>(val value: T, val setter: (T) -> Unit)

inline fun <reified T : RProps> rFunction(crossinline handler: RBuilder.(props: T) -> Unit) = { props: T ->
    buildElement {
        handler(restoreKotlinType(props))
    }
}.unsafeCast<RClass<T>>()

inline fun <reified T : RProps> restoreKotlinType(@Suppress("UNUSED_PARAMETER") props: T): T {
    @Suppress("UNUSED_VARIABLE") val jsClass = T::class.js.unsafeCast<T>()
    val newProps = js("new jsClass()")
    objectAssign(newProps, props)
    return newProps.unsafeCast<T>()
}

fun <P : RProps> RBuilder.element(clazz: RClass<P>, props: P, key: String? = null, handler: RHandler<P> = {}) {
    key?.let { props.key = it }
    child(
            type = clazz,
            props = props,
            handler = handler
    )
}

fun buildElements(handler: MagicBuilder.() -> Unit): dynamic {
    val nodes = MagicBuilder().apply(handler).childList
    return when {
        nodes.size == 0 -> null
        nodes.size == 1 -> nodes.first()
        else -> throw Exception("Did you forget to build an element?")
    }
}

class MagicBuilder : RBuilder() {

    operator fun <P : RProps> RClass<P>.invoke(props: P, key: String? = null, handler: MagicHandler<P> = {}) {
        key?.let { props.key = it }
        child(
                type = this,
                props = props,
                handler = handler
        )
    }

    private fun <P : RProps> child(type: Any, props: P, handler: MagicHandler<P>): ReactElement {
        val children = with(MagicElementBuilder(props)) {
            handler()
            childList
        }
        return child(type, props, children)
    }

}

class MagicElementBuilder<out P : RProps>(override val attrs: P) : RElementBuilder<P>(attrs) {

}

typealias MagicHandler<P> = MagicElementBuilder<P>.() -> Unit
