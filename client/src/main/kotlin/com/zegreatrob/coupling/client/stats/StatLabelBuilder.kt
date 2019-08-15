package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import react.RBuilder
import react.dom.span

object StatLabel : ComponentProvider<EmptyProps>(provider()), StatLabelBuilder

val RBuilder.statLabel: BuilderCaptor<EmptyProps> get() = StatLabel.captor(this)

interface StatLabelBuilder : StyledComponentBuilder<EmptyProps, SimpleStyle> {

    override val componentPath: String get() = "stats/StatLabel"

    override fun build() = this.buildBy {
        reactElement {
            span(classes = styles.className) { props.children() }
        }
    }
}
