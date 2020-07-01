package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import kotlinext.js.jsObject
import org.w3c.dom.Node
import react.*
import kotlin.reflect.KClass

@JsModule("react")
private external val React: dynamic

@JsModule("core-js/features/object/assign")
external fun <T, R : T> objectAssign(dest: R, vararg src: T): R

fun <T> useStateWithSetterFunction(default: T): StateValueContentWithSetterFunction<T> {
    val stateArray = React.useState(default)
    return StateValueContentWithSetterFunction(
        value = stateArray[0].unsafeCast<T>(),
        setter = stateArray[1].unsafeCast<((T) -> T) -> Unit>()
    )
}

data class StateValueContentWithSetterFunction<T>(val value: T, val setter: ((T) -> T) -> Unit)

fun <T> RBuilder.consumer(type: RConsumer<T>, children: RBuilder.(T) -> Unit) = child(
    React.createElement(type, jsObject {}) { value: T ->
        buildElement {
            children(value)
        }
    }
        .unsafeCast<ReactElement>()
)

data class StateValueContent<T>(val value: T, val setter: (T) -> Unit)

fun <P : RProps> RBuilder.child(
    clazz: RClass<P>,
    props: P,
    key: String? = null,
    ref: RReadableRef<Node>? = null,
    handler: RHandler<P> = {}
): ReactElement {
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return child(
        type = clazz,
        props = props,
        handler = handler
    )
}

inline fun <reified P : RProps> reactFunction(crossinline function: RBuilder.(P) -> Unit): RClass<P> =
    buildReactFunction(P::class) { props -> buildElement { function(props) } }

inline fun <reified P : RProps> windowReactFunc(crossinline handler: RBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions -> reactFunction<P> { handler(it, windowFunctions) } }

fun <P : RProps> buildReactFunction(kClass: KClass<P>, builder: (props: P) -> ReactElement) = { props: P ->
    ensureKotlinClassProps(props, kClass.js)
        .let(builder)
}.unsafeCast<RClass<P>>()

private fun <P : RProps> ensureKotlinClassProps(props: P, jsClass: JsClass<P>): P = if (props::class.js == jsClass) {
    props
} else {
    val newProps = js("new jsClass()")
    objectAssign(newProps, props)
    newProps.unsafeCast<P>()
}

object EmptyProps : RProps

external interface SimpleStyle {
    val className: String
}

operator fun SimpleStyle.get(propertyName: String): String = let {
    @Suppress("UNUSED_VARIABLE") val prop = propertyName
    js("it[prop]").unsafeCast<String>()
}

fun <T> useStyles(path: String): T = loadStyles(path)
fun useStyles(path: String): SimpleStyle = loadStyles(path)
