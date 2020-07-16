package com.zegreatrob.coupling.client.external.react

import org.w3c.dom.Node
import react.*

class BuilderAdapter<P : RProps>(private val builder: RBuilder, private val component: RClass<P>) {
    operator fun invoke(props: P, key: String? = null, ref: RReadableRef<Node>? = null, handler: RHandler<P> = {}) =
        builder.child(component, props, key, ref, handler)
}

operator fun BuilderAdapter<EmptyProps>.invoke(
    key: String? = null,
    ref: RReadableRef<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) = invoke(EmptyProps, key, ref, handler)

fun <P : RProps> RBuilder.builder(component: RClass<P>) = BuilderAdapter(this, component)
