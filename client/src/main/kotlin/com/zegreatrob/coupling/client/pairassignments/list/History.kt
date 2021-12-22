package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.react.windowReactFunc
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.PinSection
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import react.RBuilder
import react.dom.attrs
import react.dom.div
import react.dom.key
import react.dom.span

private val styles = useStyles("pairassignments/History")

data class History(
    val tribe: Tribe,
    val history: List<PairAssignmentDocument>,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>
) : DataProps<History> {
    override val component = com.zegreatrob.coupling.client.pairassignments.list.history
}

val history by lazy { historyFunc(WindowFunctions) }

val historyFunc = windowReactFunc<History> { (tribe, history, controls), windowFuncs ->
    val (dispatchFunc, reload) = controls
    val onDeleteFactory = { documentId: PairAssignmentDocumentId ->
        val deleteFunc = dispatchFunc({ DeletePairAssignmentsCommand(tribe.id, documentId) }, { reload() })
        onDeleteClick(windowFuncs, deleteFunc)
    }
    div(classes = styles.className) {
        div(classes = styles["tribeBrowser"]) {
            child(TribeCard(tribe))
        }
        span(classes = styles["historyView"]) {
            div(classes = styles["header"]) { +"History!" }
            history.forEach {
                pairAssignmentRow(it, onDeleteFactory(it.id))
            }
        }
    }
}

private fun onDeleteClick(windowFunctions: WindowFunctions, deleteFunc: () -> Unit) = fun() {
    if (windowFunctions.window.confirm("Are you sure you want to delete these pair assignments?")) {
        deleteFunc.invoke()
    }
}

private fun RBuilder.pairAssignmentRow(document: PairAssignmentDocument, onDeleteClick: () -> Unit) =
    div(classes = styles["pairAssignments"]) {
        attrs { key = document.id.value }
        span(classes = styles["pairAssignmentsHeader"]) { +document.dateText() }
        deleteButton(onClickFunc = onDeleteClick)
        div { showPairs(document) }
    }

private fun RBuilder.deleteButton(onClickFunc: () -> Unit) =
    child(CouplingButton(small, red, styles["deleteButton"], onClickFunc, {}) { +"DELETE" })

private fun RBuilder.showPairs(document: PairAssignmentDocument) = document.pairs.mapIndexed { index, pair ->
    span(classes = styles["pair"]) {
        attrs { key = "$index" }
        pair.players.map { pinnedPlayer: PinnedPlayer ->
            showPlayer(pinnedPlayer)
        }
        child(PinSection(pinList = pair.pins, scale = PinButtonScale.ExtraSmall, className = styles["pinSection"]))
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
