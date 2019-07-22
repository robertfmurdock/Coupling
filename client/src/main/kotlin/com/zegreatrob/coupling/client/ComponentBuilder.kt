package com.zegreatrob.coupling.client

import react.RBuilder
import react.RProps
import react.ReactElement

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

interface StyledComponentBuilder<P : RProps, S> : ComponentBuilder<P> {

    val componentPath: String

}

inline fun <reified P : RProps, S> StyledComponentBuilder<P, S>.buildStyledComponent(crossinline builder: PropsStylesBuilder<P, S>.() -> RBuilder.() -> ReactElement) =
        styledComponent(componentPath, builder)