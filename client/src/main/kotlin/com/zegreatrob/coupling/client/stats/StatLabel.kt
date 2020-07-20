package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.EmptyProps
import com.zegreatrob.minreact.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.builder
import com.zegreatrob.coupling.client.external.react.useStyles
import react.RBuilder
import react.dom.span

private val styles = useStyles("stats/StatLabel")

val StatLabel = reactFunction<EmptyProps> { props ->
    span(classes = styles.className) { props.children() }
}

val RBuilder.statLabel get() = this.builder(StatLabel)
