package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.react.windowReactFunc
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.pinSection
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.key
import react.dom.span

private val styles = useStyles("pairassignments/History")

data class HistoryProps(
    val tribe: Tribe,
    val history: List<PairAssignmentDocument>,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>
) : RProps

val History by lazy { historyComponent(WindowFunctions) }

val historyComponent = windowReactFunc<HistoryProps> { (tribe, history, controls), windowFuncs ->
    val onDeleteFunc = onDeleteFuncFactory(controls, tribe, windowFuncs)
    div(classes = styles.className) {
        div(classes = styles["tribeBrowser"]) {
            tribeCard(TribeCardProps(tribe))
        }
        span(classes = styles["historyView"]) {
            div(classes = styles["header"]) { +"History!" }
            history.forEach {
                pairAssignmentRow(it, onDeleteFunc)
            }
        }
    }
}

private fun onDeleteFuncFactory(
    controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    tribe: Tribe,
    windowFunctions: WindowFunctions
) = { documentId: PairAssignmentDocumentId ->
    val (dispatchFunc, _, reload) = controls
    val deleteFunc = dispatchFunc({ DeletePairAssignmentsCommand(tribe.id, documentId) }, { reload() })
    onDeleteClick(windowFunctions, deleteFunc)
}

private fun onDeleteClick(windowFuncs: WindowFunctions, deleteFunc: () -> Unit) = fun() {
    if (windowFuncs.window.confirm("Are you sure you want to delete these pair assignments?")) {
        deleteFunc.invoke()
    }
}

private fun RBuilder.pairAssignmentRow(
    document: PairAssignmentDocument,
    onDeleteFunc: (PairAssignmentDocumentId) -> () -> Unit
) = div(classes = styles["pairAssignments"]) {
    attrs { key = document.id.value }
    span(classes = styles["pairAssignmentsHeader"]) { +document.dateText() }
    deleteButton(onClickFunc = onDeleteFunc(document.id))
    div { showPairs(document) }
}

private fun RBuilder.deleteButton(onClickFunc: () -> Unit) =
    couplingButton(small, red, styles["deleteButton"], onClickFunc) {
        +"DELETE"
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

private fun RBuilder.showPlayer(pinnedPlayer: PinnedPlayer) = span(classes = styles["player"]) {
    attrs { key = pinnedPlayer.player.id }
    div(classes = styles["playerHeader"]) {
        +pinnedPlayer.player.name
    }
}

fun PairAssignmentDocument.dateText() = date.local.dateText()

private fun DateTimeTz.dateText() = "${format(DateFormat("MM/dd/YYYY"))} - ${format(DateFormat("HH:mm:ss"))}"
