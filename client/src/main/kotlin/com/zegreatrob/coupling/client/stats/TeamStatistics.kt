package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class TeamStatistics(
    val spinsUntilFullRotation: Int,
    val activePlayerCount: Int,
    val medianSpinDuration: String
) : DataProps<TeamStatistics> {
    override val component: TMFC<TeamStatistics> = teamStatistics
}

private val styles = useStyles("stats/TeamStatistics")

val teamStatistics = tmFC<TeamStatistics> { (spinsUntilFullRotation, activePlayerCount, medianSpinDuration) ->
    div {
        className = styles.className
        StatsHeader { +"Team Stats" }
        div {
            StatLabel { +"Spins Until Full Rotation:" }
            span { className = styles["rotationNumber"]; +"$spinsUntilFullRotation" }
        }
        div {
            StatLabel { +"Number of Active Players:" }
            span { className = styles["activePlayerCount"]; +"$activePlayerCount" }
        }
        div {
            StatLabel { +"Median Spin Duration:" }
            span { className = styles["medianSpinDuration"]; +medianSpinDuration }
        }
    }
}
