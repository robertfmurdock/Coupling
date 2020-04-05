package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.invoke
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.span

val RBuilder.teamStatistics get() = TeamStatistics.render(this)

data class TeamStatisticsProps(
    val spinsUntilFullRotation: Int,
    val activePlayerCount: Int,
    val medianSpinDuration: String
) : RProps

private val styles = useStyles("stats/TeamStatistics")

val TeamStatistics = reactFunction<TeamStatisticsProps> { props ->
    val (spinsUntilFullRotation, activePlayerCount, medianSpinDuration) = props
    div(classes = styles.className) {
        statsHeader { +"Team Stats" }
        div {
            statLabel { +"Spins Until Full Rotation:" }
            span(classes = styles["rotationNumber"]) { +"$spinsUntilFullRotation" }
        }
        div {
            statLabel { +"Number of Active Players:" }
            span(classes = styles["activePlayerCount"]) { +"$activePlayerCount" }
        }
        div {
            statLabel { +"Median Spin Duration:" }
            span(classes = styles["medianSpinDuration"]) { +medianSpinDuration }
        }
    }
}
