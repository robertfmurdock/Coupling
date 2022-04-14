package com.zegreatrob.coupling.client.pairassignments.list

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.react.windowReactFunc
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.pin.PinSection
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.key

private val styles = useStyles("pairassignments/History")

data class History(
    val party: Party,
    val history: List<PairAssignmentDocument>,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>
) : DataPropsBind<History>(com.zegreatrob.coupling.client.pairassignments.list.history)

val history by lazy { historyFunc(WindowFunctions) }

val historyFunc = windowReactFunc<History> { (party, history, controls), windowFuncs ->
    val (dispatchFunc, reload) = controls
    val onDeleteFactory = { documentId: PairAssignmentDocumentId ->
        val deleteFunc = dispatchFunc({ DeletePairAssignmentsCommand(party.id, documentId) }, { reload() })
        onDeleteClick(windowFuncs, deleteFunc)
    }
    div {
        className = styles.className
        ConfigHeader {
            this.party = party
            +"History!"
        }
        span {
            className = styles["historyView"]
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

private fun ChildrenBuilder.pairAssignmentRow(document: PairAssignmentDocument, onDeleteClick: () -> Unit) = div {
    className = styles["pairAssignments"]
    key = document.id.value
    span {
        className = styles["pairAssignmentsHeader"]
        +document.dateText()
    }
    deleteButton(onClickFunc = onDeleteClick)
    showPairs(document)
}

private fun ChildrenBuilder.deleteButton(onClickFunc: () -> Unit) =
    child(CouplingButton(small, red, styles["deleteButton"], onClickFunc)) {
        +"DELETE"
    }

private fun ChildrenBuilder.showPairs(document: PairAssignmentDocument) = div {
    document.pairs.mapIndexed { index, pair ->
        span {
            className = styles["pair"]
            key = "$index"
            pair.players.map { pinnedPlayer: PinnedPlayer ->
                showPlayer(pinnedPlayer)
            }
            child(PinSection(pinList = pair.pins, scale = PinButtonScale.ExtraSmall, className = styles["pinSection"]))
        }
    }
}

private fun ChildrenBuilder.showPlayer(pinnedPlayer: PinnedPlayer) = span {
    className = styles["player"]
    key = pinnedPlayer.player.id
    div {
        className = styles["playerHeader"]
        +pinnedPlayer.player.name
    }
}

fun PairAssignmentDocument.dateText() = date.local.dateText()

private fun DateTimeTz.dateText() = "${format(DateFormat("MM/dd/YYYY"))} - ${format(DateFormat("HH:mm:ss"))}"
