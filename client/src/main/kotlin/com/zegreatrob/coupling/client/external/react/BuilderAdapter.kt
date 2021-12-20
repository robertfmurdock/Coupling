package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.minreact.EmptyProps
import org.w3c.dom.Node
import react.*

class BuilderAdapter<P : Props>(val builder: RBuilder, val component: ElementType<P>) {
    operator fun invoke(props: P, key: String? = null, ref: MutableRefObject<Node>? = null, handler: RHandler<P> = {}) {
        key?.let { props.key = it }
        ref?.let { props.ref = ref }
        return builder.child(
            type = component,
            props = props,
            handler = handler
        )
    }
}

operator fun BuilderAdapter<EmptyProps>.invoke(
    key: String? = null,
    ref: MutableRefObject<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) {
    val props = EmptyProps()
    key?.let { props.key = it }
    ref?.let { props.ref = ref }
    return builder.child(
        type = component,
        props = props,
        handler = handler
    )
}

fun <P : Props> RBuilder.childCurry(component: ElementType<P>) = BuilderAdapter(this, component)
