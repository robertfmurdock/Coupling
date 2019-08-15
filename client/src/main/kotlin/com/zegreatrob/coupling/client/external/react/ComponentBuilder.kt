package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.loadStyles
import react.RProps
import react.ReactElement

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

interface SimpleComponentBuilder<P : RProps> : ComponentBuilder<P>

interface SimpleComponentRenderer<P : RProps> : SimpleComponentBuilder<P>, PropsClassProvider<P> {
    fun RContext<P>.render(): ReactElement
    override fun build() = functionFromRender()
}

fun <P : RProps, B> B.functionFromRender() where B : SimpleComponentRenderer<P>, B : PropsClassProvider<P> =
    ReactFunctionComponent(kClass) { props: P ->
        RContext(props)
            .run { render() }
    }

interface StyledComponentBuilder<P : RProps, S> : ComponentBuilder<P> {
    val componentPath: String
}

interface StyledComponentRenderer<P : RProps, S> : StyledComponentBuilder<P, S>, PropsClassProvider<P> {
    fun StyledRContext<P, S>.render(): ReactElement

    override fun build() = functionFromRender(loadStyles(componentPath))

    private fun functionFromRender(styles: S) = ReactFunctionComponent(kClass) { props: P ->
        StyledRContext(props, styles)
            .run { render() }
    }
}

inline fun <reified P : RProps, S> StyledComponentBuilder<P, S>.buildBy(crossinline builder: StyledRContext<P, S>.() -> ReactElement) =
    styledComponent(componentPath, builder)


inline fun <reified P : RProps, S, B> B.functionFromRender()
        where B : StyledComponentBuilder<P, S>, B : StyledComponentRenderer<P, S> = buildBy { render() }

interface ScopedStyledComponentBuilder<P : RProps, S> : ComponentBuilder<P>, ScopeProvider {
    val componentPath: String
}

inline fun <reified P : RProps, S> ScopedStyledComponentBuilder<P, S>.buildBy(crossinline builder: ScopedStyledRContext<P, S>.() -> ReactElement) =
    styledComponent(componentPath, builder)