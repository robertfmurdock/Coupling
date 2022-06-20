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
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
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
        css(styles.className) {
            display = Display.inlineBlock
            textAlign = TextAlign.left
            whiteSpace = csstype.WhiteSpace.normal
        }

        pairReports.mapIndexed { index, pairReport ->
            pairReport(index, pairReport)
        }
    }
}

private fun ChildrenBuilder.pairReport(index: Int, pairReport: PairReport) = div {
    css(styles["pairReport"]) {
        borderWidth = 2.px
        borderStyle = LineStyle.solid
        borderColor = Color("#8e8e8e")
        borderRadius = 5.px
        backgroundColor = Color("#ffffff")
        margin = 2.px
    }
    key = "$index"
    pairReport.pair.asArray().map { player -> reportPlayerCard(player) }

    div {
        css {
            display = Display.inlineBlock
            verticalAlign = VerticalAlign.top
            margin = 8.px
        }
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
    css {
        display = Display.inlineBlock
    }
    key = player.id
    add(PlayerCard(player, size = 50))
}
