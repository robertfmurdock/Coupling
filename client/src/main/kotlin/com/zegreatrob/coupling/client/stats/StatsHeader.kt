package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import react.RBuilder
import react.RHandler
import react.dom.div

interface StatsHeaderBuilder : ComponentBuilder<EmptyProps> {
    override fun build(): ReactFunctionComponent<EmptyProps> = styledComponent("stats/StatsHeader")
    { props, styles: SimpleStyle ->
        div(classes = styles.className) { props.children() }
    }

}

interface StatsHeaderSyntax {
    companion object : StatsHeaderBuilder {
        val component = build()
    }

    fun RBuilder.statsHeader(handler: RHandler<EmptyProps>) = component(component, EmptyProps, handler = handler)
}

