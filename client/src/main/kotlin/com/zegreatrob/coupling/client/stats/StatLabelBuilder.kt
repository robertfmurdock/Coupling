package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import react.RBuilder
import react.dom.span

object StatLabel : ComponentProvider<EmptyProps>(), StatLabelBuilder

val RBuilder.statLabel: BuilderCaptor<EmptyProps> get() = StatLabel.captor(this)

interface StatLabelBuilder : StyledComponentBuilder<EmptyProps, SimpleStyle> {

    override val componentPath: String get() = "stats/StatLabel"

    override fun build() = buildBy {
        {
            span(classes = styles.className) { props.children() }
        }
    }
}
