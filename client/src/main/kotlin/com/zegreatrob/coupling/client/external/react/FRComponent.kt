package com.zegreatrob.coupling.client.external.react

import react.RProps
import react.ReactElement

abstract class FRComponent<P : RProps>(provider: PropsClassProvider<P>) : RComponent<P>(provider) {
    abstract fun render(props: P): ReactElement
    override fun build() =
        ReactFunctionComponent(kClass) { render(it) }
}