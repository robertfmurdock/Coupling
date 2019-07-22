package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.*
import react.RBuilder
import react.dom.span

object StatLabel : ComponentProvider<EmptyProps>(), StatLabelBuilder

val RBuilder.statLabel: BuilderCaptor<EmptyProps> get() = StatLabel.captor(this)

interface StatLabelBuilder : ComponentBuilder<EmptyProps> {
    override fun build(): ReactFunctionComponent<EmptyProps> = styledComponent<EmptyProps, SimpleStyle>("stats/StatLabel")
    {
        {
            span(classes = styles.className) { props.children() }
        }
    }
}
