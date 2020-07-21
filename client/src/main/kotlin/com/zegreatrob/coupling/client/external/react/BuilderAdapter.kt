package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.minreact.EmptyProps
import com.zegreatrob.minreact.child
import org.w3c.dom.Node
import react.*

class BuilderAdapter<P : RProps>(val builder: RBuilder, val component: RClass<P>) {
    operator fun invoke(props: P, key: String? = null, ref: RMutableRef<Node?>? = null, handler: RHandler<P> = {}) =
        builder.child(component, props, key, ref, handler)
}

operator fun BuilderAdapter<EmptyProps>.invoke(
    key: String? = null,
    ref: RMutableRef<Node?>? = null,
    handler: RHandler<EmptyProps> = {}
) = builder.child(component, key, ref, handler)

fun <P : RProps> RBuilder.childCurry(component: RClass<P>) = BuilderAdapter(this, component)
