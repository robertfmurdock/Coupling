package com.zegreatrob.coupling.client.external.react

import react.RProps
import react.ReactElement

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

interface SimpleComponentBuilder<P : RProps> : ComponentBuilder<P>

inline fun <reified P : RProps> SimpleComponentBuilder<P>.buildBy(crossinline builder: PropsBuilder<P>.() -> ReactElement) =
    reactFunctionComponent { props: P ->
        PropsBuilder(props)
            .handle(builder)
    }

interface StyledComponentBuilder<P : RProps, S> : ComponentBuilder<P> {
    val componentPath: String
}

inline fun <reified P : RProps, S> StyledComponentBuilder<P, S>.buildBy(crossinline builder: PropsStylesBuilder<P, S>.() -> ReactElement) =
    styledComponent(componentPath, builder)

interface ScopedStyledComponentBuilder<P : RProps, S> : ComponentBuilder<P>, ScopeProvider {
    val componentPath: String
}

inline fun <reified P : RProps, S> ScopedStyledComponentBuilder<P, S>.buildBy(crossinline builder: ScopedPropsStylesBuilder<P, S>.() -> ReactElement) =
    styledComponent(componentPath, builder)