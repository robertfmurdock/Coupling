package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.zegreatrob.coupling.action.ScopeProvider
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.PinSection.pinSection
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.key
import react.dom.span

object History : RComponent<HistoryProps>(provider()), HistoryRenderer, Sdk by SdkSingleton

external interface HistoryStyles {
    val pair: String
    val tribeBrowser: String
    val historyView: String
    val header: String
    val player: String
    val playerHeader: String
    val deleteButton: String
    val pairAssignments: String
    val pairAssignmentsHeader: String
    val pinSection: String
}

data class HistoryProps(
    val tribe: Tribe,
    val history: List<PairAssignmentDocument>,
    val reload: () -> Unit,
    val pathSetter: (String) -> Unit
) : RProps

interface HistoryRenderer : ScopedStyledComponentRenderer<HistoryProps, HistoryStyles>,
    DeletePairAssignmentsCommandDispatcher,
    WindowFunctions,
    ScopeProvider {

    override val componentPath: String get() = "pairassignments/History"

    override fun ScopedStyledRContext<HistoryProps, HistoryStyles>.render() = with(props) {
        reactElement {
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

    private fun RBuilder.pairAssignmentList(props: HistoryProps, scope: CoroutineScope, styles: HistoryStyles) =
        props.history.forEach {
            val pairAssignmentDocumentId = it.id ?: return@forEach

            div(classes = styles.pairAssignments) {
                attrs { key = pairAssignmentDocumentId.value }
                span(classes = styles.pairAssignmentsHeader) { +it.dateText() }
                deleteButton(styles, scope, pairAssignmentDocumentId, props)
                div { showPairs(it, styles) }
            }
        }

    private fun RBuilder.deleteButton(
        styles: HistoryStyles,
        scope: CoroutineScope,
        pairAssignmentDocumentId: PairAssignmentDocumentId,
        props: HistoryProps
    ) {
        span(classes = "small red button") {
            attrs {
                classes += styles.deleteButton
                onClickFunction = { _ ->
                    scope.launch {
                        removeButtonOnClick(pairAssignmentDocumentId, props.tribe.id, props.reload)
                    }
                }
            }
            +"DELETE"
        }
    }

    private suspend fun removeButtonOnClick(
        pairAssignmentDocumentId: PairAssignmentDocumentId,
        tribeId: TribeId,
        reload: () -> Unit
    ) {
        if (window.confirm("Are you sure you want to delete these pair assignments?")) {
            DeletePairAssignmentsCommand(tribeId, pairAssignmentDocumentId).perform()
            reload()
        }
    }

    private fun RBuilder.showPairs(document: PairAssignmentDocument, styles: HistoryStyles) =
        document.pairs.mapIndexed { index, pair ->
            span(classes = styles.pair) {
                attrs { key = "$index" }
                pair.players.map { pinnedPlayer: PinnedPlayer ->
                    showPlayer(styles, pinnedPlayer)
                }
                pinSection(pair, PinButtonScale.ExtraSmall, styles.pinSection)
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

fun PairAssignmentDocument.dateText() =
    "${date.format(DateFormat("MM/dd/YYYY"))} - ${date.format(DateFormat("HH:mm:ss"))}"
