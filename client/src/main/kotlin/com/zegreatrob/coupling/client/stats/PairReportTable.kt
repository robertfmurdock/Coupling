package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ComponentBuilder
import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.PlayerCardRenderer
import com.zegreatrob.coupling.client.styledComponent
import com.zegreatrob.coupling.common.PairReport
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.DIV
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.span

interface PairReportTableSyntax {
    companion object : PairReportTableBuider {
        val component = build()
    }

    fun RBuilder.pairReportTable(props: PairReportTableProps) = component(component, props)
}

interface PairReportTableBuider :
        ComponentBuilder<PairReportTableProps>,
        PlayerCardRenderer,
        StatsHeaderSyntax,
        StatLabelSyntax {

    override fun build() = styledComponent("stats/pairReportTable")
    { props: PairReportTableProps, styles: PairReportTableStyles ->
        val (tribe, pairReports) = props
        div(classes = styles.className) {
            pairReports.mapIndexed { index, pairReport ->
                pairReport(styles, index, pairReport, tribe)
            }
        }
    }

    private fun RBuilder.pairReport(
            styles: PairReportTableStyles,
            index: Int,
            pairReport: PairReport,
            tribe: KtTribe
    ) = div(classes = styles.pairReport) {
        attrs { key = "$index"; classes += "react-pair-report" }
        pairReport.pair.asArray().map { player -> reportPlayerCard(styles, player, tribe) }

        div(classes = styles.pairStatistics) {
            statsHeader { +"Stats" }
            statLabel { +"Spins since last paired:" }
            span(classes = "time-since-last-pairing") { +"${pairReport.timeSinceLastPair}" }
        }
    }

    private fun RDOMBuilder<DIV>.reportPlayerCard(styles: PairReportTableStyles, player: Player, tribe: KtTribe) =
            div(classes = styles.playerCard) {
                attrs { key = player.id ?: "" }
                playerCard(PlayerCardProps(tribe.id, player, size = 50, pathSetter = {}))
            }
}

external interface PairReportTableStyles {
    val className: String
    val pairReport: String
    val playerCard: String
    val pairStatistics: String
}

data class PairReportTableProps(val tribe: KtTribe, val pairReports: List<PairReport>) : RProps
