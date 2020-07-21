package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.EmptyProps
import com.zegreatrob.coupling.client.external.react.childFunc
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.dom.div

val RBuilder.statsHeader get() = childFunc(StatsHeader)

private val styles = useStyles("stats/StatsHeader")

val StatsHeader = reactFunction<EmptyProps> { props ->
    div(classes = styles.className) { props.children() }
}
