package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.childCurry
import com.zegreatrob.coupling.client.external.react.useStyles
import react.PropsWithChildren
import react.RBuilder
import react.dom.span
import react.fc

private val styles = useStyles("stats/StatLabel")

val StatLabel = fc<PropsWithChildren> { props ->
    span(classes = styles.className) { props.children() }
}

val RBuilder.statLabel get() = childCurry(StatLabel)
