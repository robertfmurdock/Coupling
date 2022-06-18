package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.PairReport
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.key

private val styles = useStyles("stats/PairReportTable")

data class PairReportTable(val pairReports: List<PairReport>) : DataPropsBind<PairReportTable>(
    pairReportTable
)

val pairReportTable = tmFC<PairReportTable> { (pairReports) ->
    div {
        this.className = emotion.css.ClassName(styles.className) {
            whiteSpace = csstype.WhiteSpace.normal
        }

        pairReports.mapIndexed { index, pairReport ->
            pairReport(index, pairReport)
        }
    }
}

private fun ChildrenBuilder.pairReport(index: Int, pairReport: PairReport) = div {
    className = styles["pairReport"]
    key = "$index"
    pairReport.pair.asArray().map { player -> reportPlayerCard(player) }

    div {
        className = styles["pairStatistics"]
        StatsHeader { +"Stats" }
        StatLabel { +"Spins since last paired:" }
        span {
            className = ClassName("time-since-last-pairing")
            +pairReport.timeSinceLastPair.presentationString()
        }
    }
}

private fun TimeResult.presentationString() = when (this) {
    is TimeResultValue -> "$time"
    NeverPaired -> "Never Paired"
}

private fun ChildrenBuilder.reportPlayerCard(player: Player) = div {
    className = styles["playerCard"]
    key = player.id
    child(PlayerCard(player, size = 50))
}
