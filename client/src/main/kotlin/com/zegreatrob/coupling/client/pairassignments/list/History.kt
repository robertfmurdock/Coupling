package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.zegreatrob.coupling.action.ScopeProvider
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.pinSection
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.key
import react.dom.span


private val styles = useStyles("pairassignments/History")

data class HistoryProps(
    val tribe: Tribe,
    val history: List<PairAssignmentDocument>,
    val reload: () -> Unit,
    val pathSetter: (String) -> Unit,
    val commandDispatcher: DeletePairAssignmentsCommandDispatcher
) : RProps

open class History(scopeProvider: ScopeProvider, windowFunctions: WindowFunctions) :
    FRComponent<HistoryProps>(provider()),
    ReactScopeProvider,
    WindowFunctions by windowFunctions,
    ScopeProvider by scopeProvider {

    companion object : History(object : ScopeProvider {}, object : WindowFunctions {});

    override fun render(props: HistoryProps) = reactElement {
        val (tribe, _, _, pathSetter, _) = props
        val scope = useScope(styles.className)
        div(classes = styles.className) {
            div(classes = styles["tribeBrowser"]) {
                tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
            }
            span(classes = styles["historyView"]) {
                div(classes = styles["header"]) { +"History!" }
                pairAssignmentList(props, scope)
            }
        }
    }

    private fun RBuilder.pairAssignmentList(props: HistoryProps, scope: CoroutineScope) =
        props.history.forEach {
            val pairAssignmentDocumentId = it.id ?: return@forEach

            div(classes = styles["pairAssignments"]) {
                attrs { key = pairAssignmentDocumentId.value }
                span(classes = styles["pairAssignmentsHeader"]) { +it.dateText() }
                deleteButton(scope) {
                    props.commandDispatcher.removeButtonOnClick(
                        pairAssignmentDocumentId, props.tribe.id, props.reload
                    )
                }
                div { showPairs(it) }
            }
        }

    private fun RBuilder.deleteButton(scope: CoroutineScope, onClick: suspend CoroutineScope.() -> Unit) =
        span(classes = "small red button") {
            attrs {
                classes += styles["deleteButton"]
                onClickFunction = { scope.launch(block = onClick) }
            }
            +"DELETE"
        }

    private suspend fun DeletePairAssignmentsCommandDispatcher.removeButtonOnClick(
        pairAssignmentDocumentId: PairAssignmentDocumentId,
        tribeId: TribeId,
        reload: () -> Unit
    ) {
        if (window.confirm("Are you sure you want to delete these pair assignments?")) {
            DeletePairAssignmentsCommand(tribeId, pairAssignmentDocumentId).perform()
            reload()
        }
    }

    private fun RBuilder.showPairs(document: PairAssignmentDocument) = document.pairs.mapIndexed { index, pair ->
        span(classes = styles["pair"]) {
            attrs { key = "$index" }
            pair.players.map { pinnedPlayer: PinnedPlayer ->
                showPlayer(pinnedPlayer)
            }
            pinSection(pinList = pair.pins, scale = PinButtonScale.ExtraSmall, className = styles["pinSection"])
        }
    }

    private fun RBuilder.showPlayer(pinnedPlayer: PinnedPlayer) =
        span(classes = styles["player"]) {
            attrs { key = "${pinnedPlayer.player.id}" }
            div(classes = styles["playerHeader"]) {
                +pinnedPlayer.player.name
            }
        }
}

fun PairAssignmentDocument.dateText() =
    "${date.format(DateFormat("MM/dd/YYYY"))} - ${date.format(DateFormat("HH:mm:ss"))}"
