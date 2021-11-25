package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.childCurry
import com.zegreatrob.coupling.client.external.react.useStyles
import react.PropsWithChildren
import react.RBuilder
import react.dom.span
import react.functionComponent

private val styles = useStyles("stats/StatLabel")

val StatLabel = functionComponent<PropsWithChildren> { props ->
    span(classes = styles.className) { props.children() }
}

val RBuilder.statLabel get() = childCurry(StatLabel)
