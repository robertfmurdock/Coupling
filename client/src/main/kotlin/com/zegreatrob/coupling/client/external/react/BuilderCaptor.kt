package com.zegreatrob.coupling.client.external.react

import org.w3c.dom.Node
import react.RBuilder
import react.RHandler
import react.RProps
import react.RReadableRef

class BuilderCaptor<P : RProps>(private val componentProvider: ComponentProvider<P>, private val rBuilder: RBuilder) {

    operator fun invoke(props: P, key: String? = null, ref: RReadableRef<Node>? = null, handler: RHandler<P> = {}) =
        with(rBuilder) {
            component(componentProvider.component, props, key, ref, handler)
        }
}

operator fun BuilderCaptor<EmptyProps>.invoke(
    key: String? = null,
    ref: RReadableRef<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) =
    this(EmptyProps, key, ref, handler)