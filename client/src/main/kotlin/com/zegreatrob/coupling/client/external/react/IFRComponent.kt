package com.zegreatrob.coupling.client.external.react

import react.RBuilder
import react.RProps

@Suppress("unused")
fun <P : RProps> IFRComponent<P>.rendererFunc(wender: RBuilder.(P) -> Any) = wender

abstract class IFRComponent<P : RProps>(provider: PropsClassProvider<P>) : RComponent<P>(provider), FComponent<P> {
    abstract val renderer: RBuilder.(P) -> Any
    override fun render(props: P) =
        reactElement { renderer(props) }

    override fun build() = ReactFunctionComponent(kClass) { render(it) }
}
