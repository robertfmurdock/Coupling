package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.minreact.EmptyProps
import com.zegreatrob.minreact.child
import org.w3c.dom.Node
import react.*

class BuilderAdapter<P : Props>(val builder: RBuilder, val component: ElementType<P>) {
    operator fun invoke(props: P, key: String? = null, ref: MutableRefObject<Node>? = null, handler: RHandler<P> = {}) =
        builder.child(component, props, key, ref, handler)
}

operator fun BuilderAdapter<EmptyProps>.invoke(
    key: String? = null,
    ref: MutableRefObject<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) = builder.child(component, key, ref, handler)

fun <P : Props> RBuilder.childCurry(component: ElementType<P>) = BuilderAdapter(this, component)
