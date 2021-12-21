package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.useStyles
import react.PropsWithChildren
import react.dom.div
import react.fc

private val styles = useStyles("stats/StatsHeader")

val StatsHeader = fc<PropsWithChildren> { props ->
    div(classes = styles.className) { props.children() }
}
