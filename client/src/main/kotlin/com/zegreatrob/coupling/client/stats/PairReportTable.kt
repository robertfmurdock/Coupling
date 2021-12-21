package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.PairReport
import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.RBuilder
import react.dom.attrs
import react.dom.div
import react.dom.span

private val styles = useStyles("stats/PairReportTable")

data class PairReportTable(val tribe: Tribe, val pairReports: List<PairReport>) : DataProps<PairReportTable> {
    override val component: TMFC<PairReportTable> = pairReportTable
}

val pairReportTable = reactFunction<PairReportTable> { (tribe, pairReports) ->
    div(classes = styles.className) {
        pairReports.mapIndexed { index, pairReport ->
            pairReport(index, pairReport, tribe)
        }
    }
}

private fun RBuilder.pairReport(index: Int, pairReport: PairReport, tribe: Tribe) = div(styles["pairReport"]) {
    attrs { key = "$index" }
    pairReport.pair.asArray().map { player -> reportPlayerCard(player, tribe) }

    div(classes = styles["pairStatistics"]) {
        StatsHeader { +"Stats" }
        StatLabel { +"Spins since last paired:" }
        span(classes = "time-since-last-pairing") {
            +pairReport.timeSinceLastPair.presentationString()
        }
    }
}

private fun TimeResult.presentationString() = when (this) {
    is TimeResultValue -> "$time"
    NeverPaired -> "Never Paired"
}

private fun RBuilder.reportPlayerCard(player: Player, tribe: Tribe) = div(styles["playerCard"]) {
    attrs { key = player.id }
    child(PlayerCard(tribe.id, player, size = 50))
}


