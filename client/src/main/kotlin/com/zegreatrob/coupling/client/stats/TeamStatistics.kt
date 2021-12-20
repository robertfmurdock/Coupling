package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import react.Props
import react.dom.div
import react.dom.span

data class TeamStatisticsProps(
    val spinsUntilFullRotation: Int,
    val activePlayerCount: Int,
    val medianSpinDuration: String
) : Props

private val styles = useStyles("stats/TeamStatistics")

val TeamStatistics =
    reactFunction<TeamStatisticsProps> { props ->
        val (spinsUntilFullRotation, activePlayerCount, medianSpinDuration) = props
        div(classes = styles.className) {
            StatsHeader { +"Team Stats" }
            div {
                StatLabel { +"Spins Until Full Rotation:" }
                span(classes = styles["rotationNumber"]) { +"$spinsUntilFullRotation" }
            }
            div {
                StatLabel { +"Number of Active Players:" }
                span(classes = styles["activePlayerCount"]) { +"$activePlayerCount" }
            }
            div {
                StatLabel { +"Median Spin Duration:" }
                span(classes = styles["medianSpinDuration"]) { +medianSpinDuration }
            }
        }
    }
