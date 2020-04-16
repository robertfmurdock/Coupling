package com.zegreatrob.coupling.client.external.react

import react.RProps
import react.ReactElement


interface FComponent<P : RProps> : ComponentBuilder<P> {
    fun render(props: P): ReactElement
}