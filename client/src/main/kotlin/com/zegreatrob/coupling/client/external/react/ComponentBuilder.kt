package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.loadStyles
import react.RProps
import react.ReactElement

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

interface SimpleComponentRenderer<P : RProps> : ComponentBuilder<P>, PropsClassProvider<P> {
    fun RContext<P>.render(): ReactElement
    override fun build() = functionFromRender()
    private fun functionFromRender() = ReactFunctionComponent(kClass) { props: P ->
        RContext(props)
            .run { render() }
    }
}

interface StyledComponentRenderer<P : RProps, S> : ComponentBuilder<P>, PropsClassProvider<P> {
    val componentPath: String
    fun StyledRContext<P, S>.render(): ReactElement
    override fun build() = functionFromRender(loadStyles(componentPath))
    private fun functionFromRender(styles: S) = ReactFunctionComponent(kClass) { props: P ->
        StyledRContext(props, styles)
            .run { render() }
    }
}

interface ScopedStyledComponentBuilder<P : RProps, S> : ComponentBuilder<P>, ScopeProvider {
    val componentPath: String
}

inline fun <reified P : RProps, S> ScopedStyledComponentBuilder<P, S>.buildBy(crossinline builder: ScopedStyledRContext<P, S>.() -> ReactElement) =
    styledComponent(componentPath, builder)