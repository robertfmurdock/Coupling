package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.DataPropsBridge
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import org.w3c.dom.Node
import react.*

inline fun <reified P : DataProps<P>> reactFunction(crossinline function: RBuilder.(P) -> Unit): TMFC<P> = tmFC { props ->
    RBuilder()
        .apply { function(props) }
        .childList
        .forEach { child(it) }
}

fun <P : Props> RBuilder.child(
    clazz: ElementType<P>,
    props: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: RHandler<P> = {}
) {
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return child(
        type = clazz,
        props = props,
        handler = handler
    )
}

fun <P : DataProps<P>> RBuilder.child(
    props: P,
    key: String? = null,
    ref: Ref<Node>? = null,
    handler: RBuilder.() -> Unit = {}
) {
    val updatedProps = props.unsafeCast<DataPropsBridge<P>>()
    key?.let { updatedProps.key = it }
    ref?.let { updatedProps.ref = ref }
    return child(
        type = props.component,
        props = updatedProps,
        handler = handler
    )
}
