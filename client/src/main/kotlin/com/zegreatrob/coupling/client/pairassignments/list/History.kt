package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.react.windowReactFunc
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.invoke
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.pinSection
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
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
    val dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>
) : RProps

val History by lazy { HistoryComponent(WindowFunctions) }

val HistoryComponent = windowReactFunc<HistoryProps> { (tribe, history, reload, pathSetter, commandFunc), windowFuncs ->
    val onDeleteFunc = onDeleteFuncFactory(commandFunc, tribe, reload, windowFuncs)

    div(classes = styles.className) {
        div(classes = styles["tribeBrowser"]) {
            tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
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
    dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>,
    tribe: Tribe,
    reload: () -> Unit,
    windowFuncs: WindowFunctions
) = { documentId: PairAssignmentDocumentId ->
    val deleteFunc = dispatchFunc({ DeletePairAssignmentsCommand(tribe.id, documentId) }, { reload() })
    onDeleteClick(windowFuncs, deleteFunc)
}

private fun onDeleteClick(windowFuncs: WindowFunctions, deleteFunc: () -> Unit): () -> Unit = {
    with(windowFuncs) {
        if (window.confirm("Are you sure you want to delete these pair assignments?")) {
            deleteFunc.invoke()
        }
    }
}

private fun RBuilder.pairAssignmentRow(
    document: PairAssignmentDocument,
    onDeleteFunc: (PairAssignmentDocumentId) -> () -> Unit
) {
    val pairAssignmentDocumentId = document.id ?: return

    div(classes = styles["pairAssignments"]) {
        attrs { key = pairAssignmentDocumentId.value }
        span(classes = styles["pairAssignmentsHeader"]) { +document.dateText() }
        deleteButton(onClickFunc = onDeleteFunc(pairAssignmentDocumentId))
        div { showPairs(document) }
    }
}

private fun RBuilder.deleteButton(onClickFunc: () -> Unit) = span(classes = "small red button") {
    attrs {
        classes += styles["deleteButton"]
        onClickFunction = { onClickFunc() }
    }
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
    attrs { key = "${pinnedPlayer.player.id}" }
    div(classes = styles["playerHeader"]) {
        +pinnedPlayer.player.name
    }
}

fun PairAssignmentDocument.dateText() = date.local.dateText()

private fun DateTimeTz.dateText() = "${format(DateFormat("MM/dd/YYYY"))} - ${format(DateFormat("HH:mm:ss"))}"
