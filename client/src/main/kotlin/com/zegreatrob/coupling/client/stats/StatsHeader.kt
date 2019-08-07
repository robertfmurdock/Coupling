package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import react.RBuilder
import react.dom.div

object StatsHeader : ComponentProvider<EmptyProps>(), StatsHeaderBuilder

val RBuilder.statsHeader get() = StatsHeader.captor(this)

interface StatsHeaderBuilder : StyledComponentBuilder<EmptyProps, SimpleStyle> {

    override val componentPath: String get() = "stats/StatsHeader"
    override fun build() = buildBy {
        {
            div(classes = styles.className) { props.children() }
        }
    }
}

