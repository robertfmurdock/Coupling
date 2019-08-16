package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import react.RBuilder
import react.dom.div

object StatsHeader : RComponent<EmptyProps>(provider()), StatsHeaderBuilder

val RBuilder.statsHeader get() = StatsHeader.render(this)

interface StatsHeaderBuilder : StyledComponentRenderer<EmptyProps, SimpleStyle> {

    override val componentPath: String get() = "stats/StatsHeader"

    override fun StyledRContext<EmptyProps, SimpleStyle>.render() = reactElement {
        div(classes = styles.className) { props.children() }
    }
}

