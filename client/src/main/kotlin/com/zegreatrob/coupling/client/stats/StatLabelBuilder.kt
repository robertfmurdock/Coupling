package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import react.RBuilder
import react.RHandler
import react.dom.span

interface StatLabelBuilder : ComponentBuilder<EmptyProps> {
    override fun build(): ReactFunctionComponent<EmptyProps> = styledComponent("stats/StatLabel")
    { props, styles: SimpleStyle ->
        span(classes = styles.className) { props.children() }
    }
}

interface StatLabelSyntax {
    companion object : StatLabelBuilder {
        val component = build()
    }

    fun RBuilder.statLabel(handler: RHandler<EmptyProps>) = component(component, EmptyProps, handler = handler)
}
