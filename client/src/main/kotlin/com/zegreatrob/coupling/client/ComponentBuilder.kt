package com.zegreatrob.coupling.client

import react.RBuilder
import react.RProps
import react.ReactElement

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

inline fun <reified P : RProps> ComponentBuilder<P>.buildByPls(crossinline builder: PropsBuilder<P>.() -> RBuilder.() -> ReactElement) =
        reactFunctionComponent { props: P ->
            PropsBuilder(props)
                    .handle(builder)()
        }

interface StyledComponentBuilder<P : RProps, S> : ComponentBuilder<P> {
    val componentPath: String
}

inline fun <reified P : RProps, S> StyledComponentBuilder<P, S>.buildBy(crossinline builder: PropsStylesBuilder<P, S>.() -> RBuilder.() -> ReactElement) =
        styledComponent(componentPath, builder)

interface ScopedStyledComponentBuilder<P : RProps, S> : ComponentBuilder<P>, ScopeProvider {
    val componentPath: String
}

inline fun <reified P : RProps, S> ScopedStyledComponentBuilder<P, S>.buildBy(crossinline builder: ScopedPropsStylesBuilder<P, S>.() -> RBuilder.() -> ReactElement) =
        styledComponent(componentPath, builder)