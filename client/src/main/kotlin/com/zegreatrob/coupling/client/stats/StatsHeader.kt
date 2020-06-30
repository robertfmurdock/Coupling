package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.EmptyProps
import com.zegreatrob.coupling.client.external.react.reactFunction2
import com.zegreatrob.coupling.client.external.react.render
import com.zegreatrob.coupling.client.external.react.useStyles
import react.RBuilder
import react.dom.div

val RBuilder.statsHeader get() = StatsHeader.render(this)

private val styles = useStyles("stats/StatsHeader")

val StatsHeader = reactFunction2<EmptyProps> { props ->
    div(classes = styles.className) { props.children() }
}
