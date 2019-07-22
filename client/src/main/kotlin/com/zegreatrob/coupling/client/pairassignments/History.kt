package com.zegreatrob.coupling.client.pairassignments

import com.soywiz.klock.DateFormat
import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.span
import kotlin.js.Promise

object History : ComponentProvider<HistoryProps>(), HistoryComponentBuilder

val RBuilder.history get() = History.captor(this)

external interface HistoryStyles {
    val tribeBrowser: String
    val historyView: String
    val header: String
    val player: String
    val playerHeader: String
    val deleteButton: String
}

data class HistoryProps(
        val tribe: KtTribe,
        val pathSetter: (String) -> Unit,
        val history: List<PairAssignmentDocument>,
        val coupling: dynamic,
        val reload: () -> Unit
) : RProps

interface HistoryComponentBuilder : ScopedStyledComponentBuilder<HistoryProps, HistoryStyles>,
        WindowFunctions,
        ScopeProvider {

    override val componentPath: String get() = "pairassignments/History"

    override fun build() = buildBy {
        val (tribe, pathSetter) = props
        {
            div {
                div(classes = styles.tribeBrowser) {
                    tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
                }
                span(classes = styles.historyView) {
                    div(classes = styles.header) { +"History!" }
                    pairAssignmentList(props, scope, styles)
                }
            }
        }
    }

    private fun RBuilder.pairAssignmentList(props: HistoryProps, scope: CoroutineScope, styles: HistoryStyles): List<Any> = props.history.map {
        div(classes = "pair-assignments") {
            attrs { key = it.id?.value ?: "" }
            span(classes = "pair-assignments-header") { +it.dateText() }
            span(classes = "small red button") {
                attrs {
                    classes += styles.deleteButton
                    onClickFunction = { _ -> scope.launch { props.removeButtonOnClick(it) } }
                }
                +"DELETE"
            }
            div { showPairs(it, styles) }
        }
    }

    private fun PairAssignmentDocument.dateText() =
            "${date.format(DateFormat("MM/dd/YYYY"))} - ${date.format(DateFormat("HH:mm:ss"))}"

    private suspend fun HistoryProps.removeButtonOnClick(document: PairAssignmentDocument) {
        if (window.confirm("Are you sure you want to delete these pair assignments?")) {
            coupling.removeAssignments(document.toJson(), tribe.id.value)
                    .unsafeCast<Promise<Unit>>()
                    .await()
            reload()
        }
    }

    private fun RBuilder.showPairs(document: PairAssignmentDocument, styles: HistoryStyles) =
            document.pairs.mapIndexed { index, pair ->
                span(classes = "pair") {
                    attrs { key = "$index" }
                    pair.players.map { pinnedPlayer: PinnedPlayer ->
                        showPlayer(styles, pinnedPlayer)
                    }
                }
            }

    private fun RBuilder.showPlayer(styles: HistoryStyles, pinnedPlayer: PinnedPlayer) =
            span(classes = styles.player) {
                attrs { key = "${pinnedPlayer.player.id}" }
                div(classes = styles.playerHeader) {
                    +(pinnedPlayer.player.name ?: "Unknown")
                }
            }
}

