package com.zegreatrob.coupling.client

import react.*
import kotlin.reflect.KClass

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

fun <P : RProps> RBuilder.element(clazz: RClass<P>, props: P, key: String? = null, handler: RHandler<P> = {}): ReactElement {
    key?.let { props.key = it }
    return child(
            type = clazz,
            props = props,
            handler = handler
    )
}

inline fun <reified P : RProps> reactFunctionComponent(noinline builder: RBuilder.(props: P) -> ReactElement) =
        ReactFunctionComponent(P::class, builder)

class ReactFunctionComponent<P : RProps>(private val clazz: KClass<P>, private val builder: RBuilder.(props: P) -> ReactElement) {
    val rFunction by kotlin.lazy {
        { props: P ->
            buildElement {
                @Suppress("UNUSED_VARIABLE") val jsClass = clazz.js.unsafeCast<P>()
                builder(if (props::class.js == jsClass) {
                    props
                } else {
                    val newProps = js("new jsClass()")
                    objectAssign(newProps, props)
                    newProps.unsafeCast<P>()
                })
            }
        }.unsafeCast<RFunction<P>>()
    }
}

fun <P : RProps> RBuilder.component(component: ReactFunctionComponent<P>, props: P, key: String? = null) =
        element(component.rFunction, props, key)