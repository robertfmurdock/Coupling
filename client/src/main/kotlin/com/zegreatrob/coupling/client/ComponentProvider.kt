package com.zegreatrob.coupling.client

import react.RBuilder
import react.RProps

abstract class ComponentProvider<P : RProps> : ComponentBuilder<P> {
    val component: ReactFunctionComponent<P> by lazy { build() }
    val captor by lazy {
        { rBuilder: RBuilder -> BuilderCaptor(this, rBuilder) }
    }
}