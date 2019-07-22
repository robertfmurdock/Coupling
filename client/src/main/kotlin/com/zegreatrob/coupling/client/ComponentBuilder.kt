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

interface ScopedStyledComponentBuilder<P : RProps, S> : ComponentBuilder<P>, ScopeProvider {
    val componentPath: String
}

inline fun <reified P : RProps, S> StyledComponentBuilder<P, S>.buildComponent(crossinline builder: PropsStylesBuilder<P, S>.() -> RBuilder.() -> ReactElement) =
        styledComponent(componentPath, builder)

inline fun <reified P : RProps, S> ScopedStyledComponentBuilder<P, S>.buildComponent(crossinline builder: ScopedPropsStylesBuilder<P, S>.() -> RBuilder.() -> ReactElement) =
        styledComponent(componentPath, builder)