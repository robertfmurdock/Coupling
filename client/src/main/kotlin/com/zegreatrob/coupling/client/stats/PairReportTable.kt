package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.PairReport
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.invoke
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.span

val RBuilder.pairReportTable get() = PairReportTable.render(this)

private val styles = useStyles("stats/PairReportTable")

data class PairReportTableProps(val tribe: Tribe, val pairReports: List<PairReport>) : RProps

val PairReportTable = reactFunction<PairReportTableProps> { (tribe, pairReports) ->
    div(classes = styles.className) {
        pairReports.mapIndexed { index, pairReport ->
            pairReport(index, pairReport, tribe)
        }
    }
}

private fun RBuilder.pairReport(index: Int, pairReport: PairReport, tribe: Tribe) = div(
    classes = styles["pairReport"]
) {
    attrs { key = "$index" }
    pairReport.pair.asArray().map { player -> reportPlayerCard(player, tribe) }

    div(classes = styles["pairStatistics"]) {
        statsHeader { +"Stats" }
        statLabel { +"Spins since last paired:" }
        span(classes = "time-since-last-pairing") {
            +pairReport.timeSinceLastPair.presentationString()
        }
    }
}

private fun TimeResult.presentationString() = when (this) {
    is TimeResultValue -> "$time"
    NeverPaired -> "Never Paired"
}

private fun RBuilder.reportPlayerCard(player: Player, tribe: Tribe) = div(classes = styles["playerCard"]) {
    attrs { key = player.id ?: "" }
    playerCard(PlayerCardProps(tribe.id, player, size = 50, pathSetter = {}))
}


