package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.PairReport
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useMemo
import web.cssom.Angle
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.deg
import web.cssom.px
import kotlin.random.Random

external interface PairReportTableProps : Props {
    var pairReports: List<PairReport>
}

@ReactFunc
val PairReportTable by nfc<PairReportTableProps> { (pairReports) ->
    div {
        css {
            display = Display.inlineBlock
            textAlign = TextAlign.left
            whiteSpace = WhiteSpace.normal
        }
        pairReports.mapIndexed { index, pairReport ->
            PairReportView(pairReport, key = "$index")
        }
    }
}

external interface PairReportViewProps : Props {
    var pairReport: PairReport
}

@ReactFunc
val PairReportView by nfc<PairReportViewProps> { (pairReport) ->
    val tweak = useMemo { Random.nextInt(8).toDouble() }
    div {
        css {
            borderWidth = 2.px
            borderStyle = LineStyle.solid
            borderColor = Color("#8e8e8e")
            borderRadius = 5.px
            backgroundColor = Color("#ffffff")
            margin = 2.px
        }
        asDynamic()["data-pair-report"] = pairReport.pair.asArray().joinToString("-") { it.name }
        reportPlayerCard(pairReport.pair.player1, (-tweak).deg)
        reportPlayerCard(pairReport.pair.player2, (tweak).deg)

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
                asDynamic()["data-time-since-last-pair"] = ""
                +pairReport.timeSinceLastPair.presentationString()
            }
        }
    }
}

private fun TimeResult.presentationString() = when (this) {
    is TimeResultValue -> "$time"
    NeverPaired -> "Never Paired"
}

private fun ChildrenBuilder.reportPlayerCard(player: Player, tilt: Angle) = div {
    css {
        display = Display.inlineBlock
    }
    key = player.id
    add(PlayerCard(player, size = 50, tilt = tilt))
}
