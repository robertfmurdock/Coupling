package com.zegreatrob.coupling.client.external.react

import react.RProps
import react.ReactElement

abstract class FRComponent<P : RProps>(provider: PropsClassProvider<P>) : RComponent<P>(provider), FComponent<P> {
    override fun build() = ReactFunctionComponent(kClass) { render(it) }
}

interface FComponent<P : RProps> : ComponentBuilder<P> {
    fun render(props: P): ReactElement
}