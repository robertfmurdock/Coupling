package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ComponentBuilder
import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.styledComponent
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.span

interface TeamStatisticsSyntax {
    companion object : TeamStatisticsBuilder {
        val component = build()
    }

    fun RBuilder.teamStatistics(props: TeamStatisticsProps) = component(component, props)
}

interface TeamStatisticsBuilder : ComponentBuilder<TeamStatisticsProps>, StatsHeaderSyntax, StatLabelSyntax {

    override fun build() = styledComponent("stats/TeamStatistics")
    { props: TeamStatisticsProps, styles: TeamStatisticsStyles ->
        div(classes = styles.className) {
            statsHeader { +"Team Stats" }
            div {
                statLabel { +"Spins Until Full Rotation:" }
                span(classes = styles.rotationNumber) { +"${props.spinsUntilFullRotation}" }
            }
            div {
                statLabel { +"Number of Active Players:" }
                span(classes = styles.activePlayerCount) { +"${props.activePlayerCount}" }
            }
            div {
                statLabel { +"Median Spin Duration:" }
                span(classes = styles.medianSpinDuration) { +props.medianSpinDuration }
            }
        }
    }
}

external interface TeamStatisticsStyles {
    val className: String
    val rotationNumber: String
    val activePlayerCount: String
    val medianSpinDuration: String
}

data class TeamStatisticsProps(
        val spinsUntilFullRotation: Int,
        val activePlayerCount: Int,
        val medianSpinDuration: String
) : RProps
