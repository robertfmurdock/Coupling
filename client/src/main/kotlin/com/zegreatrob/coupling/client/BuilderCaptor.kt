package com.zegreatrob.coupling.client

import react.RBuilder
import react.RHandler
import react.RProps

class BuilderCaptor<P : RProps>(val componentProvider: ComponentProvider<P>, val rBuilder: RBuilder) {

    operator fun invoke(props: P, key: String? = null, handler: RHandler<P> = {}) = with(rBuilder) {
        component(componentProvider.component, props, key, handler)
    }
}

operator fun BuilderCaptor<EmptyProps>.invoke(key: String? = null, handler: RHandler<EmptyProps> = {}) =
        this(EmptyProps, key, handler)