package com.zegreatrob.coupling.client.external.react

import react.RProps
import react.ReactElement

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

interface SimpleComponentBuilder<P : RProps> : ComponentBuilder<P>

interface SimpleComponentRenderer<P : RProps> : SimpleComponentBuilder<P> {
    fun PropsBuilder<P>.render(): ReactElement
}

inline fun <reified P : RProps, B : SimpleComponentRenderer<P>> B.functionFromRender() =
    reactFunctionComponent { props: P ->
        PropsBuilder(props)
            .run { render() }
    }

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

interface StyledComponentRenderer<P : RProps, S> {
    fun PropsStylesBuilder<P, S>.render(): ReactElement
}

inline fun <reified P : RProps, S, B> B.functionFromRender()
        where B : StyledComponentBuilder<P, S>, B : StyledComponentRenderer<P, S> = buildBy { render() }

interface ScopedStyledComponentBuilder<P : RProps, S> : ComponentBuilder<P>, ScopeProvider {
    val componentPath: String
}

inline fun <reified P : RProps, S> ScopedStyledComponentBuilder<P, S>.buildBy(crossinline builder: ScopedPropsStylesBuilder<P, S>.() -> ReactElement) =
    styledComponent(componentPath, builder)