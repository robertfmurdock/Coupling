package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import react.RBuilder
import react.dom.span

object StatLabel : RComponent<EmptyProps>(provider()), StatLabelBuilder

val RBuilder.statLabel: RenderToBuilder<EmptyProps> get() = StatLabel.render(this)

interface StatLabelBuilder : StyledComponentRenderer<EmptyProps, SimpleStyle> {

    override val componentPath: String get() = "stats/StatLabel"

    override fun StyledRContext<EmptyProps, SimpleStyle>.render() = reactElement {
        span(classes = styles.className) { props.children() }
    }
}
