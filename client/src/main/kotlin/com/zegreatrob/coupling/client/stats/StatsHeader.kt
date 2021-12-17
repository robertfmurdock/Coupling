package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.childCurry
import com.zegreatrob.coupling.client.external.react.useStyles
import react.PropsWithChildren
import react.RBuilder
import react.dom.div
import react.fc

val RBuilder.statsHeader get() = childCurry(StatsHeader)

private val styles = useStyles("stats/StatsHeader")

val StatsHeader = fc<PropsWithChildren> { props ->
    div(classes = styles.className) { props.children() }
}
