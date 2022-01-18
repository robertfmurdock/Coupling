package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.PairReport
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.key

private val styles = useStyles("stats/PairReportTable")

data class PairReportTable(val tribe: Tribe, val pairReports: List<PairReport>) : DataProps<PairReportTable> {
    override val component: TMFC<PairReportTable> = pairReportTable
}

val pairReportTable = tmFC<PairReportTable> { (tribe, pairReports) ->
    div {
        className = styles.className
        pairReports.mapIndexed { index, pairReport ->
            pairReport(index, pairReport, tribe)
        }
    }
}

private fun ChildrenBuilder.pairReport(index: Int, pairReport: PairReport, tribe: Tribe) = div {
    className = styles["pairReport"]
     key = "$index"
    pairReport.pair.asArray().map { player -> reportPlayerCard(player, tribe) }

    div {
        className = styles["pairStatistics"]
        StatsHeader { +"Stats" }
        StatLabel { +"Spins since last paired:" }
        span {
            className = "time-since-last-pairing"
            +pairReport.timeSinceLastPair.presentationString()
        }
    }
}

private fun TimeResult.presentationString() = when (this) {
    is TimeResultValue -> "$time"
    NeverPaired -> "Never Paired"
}

private fun ChildrenBuilder.reportPlayerCard(player: Player, tribe: Tribe) = div {
    className = styles["playerCard"]
    key = player.id
    child(PlayerCard(tribe.id, player, size = 50))
}
