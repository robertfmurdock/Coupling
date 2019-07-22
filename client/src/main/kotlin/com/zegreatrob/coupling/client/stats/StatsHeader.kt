package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import react.RBuilder
import react.dom.div

object StatsHeader : ComponentProvider<EmptyProps>(), StatsHeaderBuilder

val RBuilder.statsHeader get() = StatsHeader.captor(this)

interface StatsHeaderBuilder : ComponentBuilder<EmptyProps> {
    override fun build(): ReactFunctionComponent<EmptyProps> = styledComponent("stats/StatsHeader")
    { props, styles: SimpleStyle ->
        div(classes = styles.className) { props.children() }
    }

}

