package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.EmptyProps
import com.zegreatrob.minreact.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.builder
import com.zegreatrob.coupling.client.external.react.useStyles
import react.RBuilder
import react.dom.div

val RBuilder.statsHeader get() = this.builder(StatsHeader)

private val styles = useStyles("stats/StatsHeader")

val StatsHeader = reactFunction<EmptyProps> { props ->
    div(classes = styles.className) { props.children() }
}
