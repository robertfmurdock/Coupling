package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.action.ScopeProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
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
    override fun build() = loadStyles<S>(componentPath).toFunctionComponent()

    private fun S.toFunctionComponent() = ReactFunctionComponent(kClass) { props: P ->
        StyledRContext(props, this)
            .run { render() }
    }
}

interface ScopedStyledComponentRenderer<P : RProps, S> : ComponentBuilder<P>, ReactScopeProvider, PropsClassProvider<P> {
    val componentPath: String

    fun ScopedStyledRContext<P, S>.render(): ReactElement

    override fun build() = loadStyles<S>(componentPath).toFunctionComponent()

    private fun S.toFunctionComponent() = ReactFunctionComponent(kClass) { props: P ->
        val scope = useScope(componentPath)
        ScopedStyledRContext(props, this, scope)
            .handle { render() }
    }
}

interface ReactScopeProvider : ScopeProvider {
    fun useScope(coroutineName: String): CoroutineScope {
        val (scope) = useState { buildScope() + CoroutineName(coroutineName) }
        useEffectWithCleanup(arrayOf()) {
            { scope.cancel() }
        }
        return scope
    }
}