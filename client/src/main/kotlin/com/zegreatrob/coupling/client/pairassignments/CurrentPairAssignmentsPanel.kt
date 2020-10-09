package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.currentPairs
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RProps
import react.dom.div

fun RBuilder.currentPairAssignments(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    onPlayerSwap: SwapCallback,
    onPinDrop: PinMoveCallback,
    allowSave: Boolean,
    controls: Controls<PairAssignmentsCommandDispatcher>
) = child(
    CurrentPairAssignmentsPanel,
    CurrentPairAssignmentsPanelProps(tribe, pairAssignments, onPlayerSwap, onPinDrop, allowSave, controls)
)

data class CurrentPairAssignmentsPanelProps(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument,
    val onPlayerSwap: SwapCallback,
    val onPinDrop: PinMoveCallback,
    val allowSave: Boolean,
    val controls: Controls<PairAssignmentsCommandDispatcher>
) : RProps

private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

val CurrentPairAssignmentsPanel = reactFunction<CurrentPairAssignmentsPanelProps> { props ->
    val (tribe, pairAssignments, onPlayerSwap, onPinDrop, allowSave, controls) = props
    div(classes = styles.className) {
        dateHeader(pairAssignments)
        pairAssignmentList(tribe, pairAssignments, onPlayerSwap, onPinDrop, allowSave, controls.pathSetter)
        if (allowSave) {
            controlSection(tribe, pairAssignments, controls)
        }
    }
}

private fun RBuilder.dateHeader(pairAssignments: PairAssignmentDocument) = div {
    div {
        pairAssignmentsHeader(pairAssignments)
    }
}

private fun RBuilder.pairAssignmentList(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    onPlayerSwap: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
    onPinDrop: (String, PinnedCouplingPair) -> Unit,
    allowSave: Boolean,
    pathSetter: (String) -> Unit
) = div(classes = styles["pairAssignmentsContent"]) {
    pairAssignments.pairs.mapIndexed { index, pair ->
        assignedPair(tribe, pair, onPlayerSwap, onPinDrop, allowSave, pathSetter, key = "$index")
    }
}

private fun RBuilder.controlSection(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    controls: Controls<PairAssignmentsCommandDispatcher>
) = div {
    val (dispatchFunc, pathSetter, _) = controls
    val redirectToCurrentFunc: (Result<Unit>) -> Unit = { pathSetter.currentPairs(tribe.id) }
    saveButton(dispatchFunc({ SavePairAssignmentsCommand(tribe.id, pairAssignments) }, redirectToCurrentFunc))
    cancelButton(dispatchFunc({ DeletePairAssignmentsCommand(tribe.id, pairAssignments.id!!) }, redirectToCurrentFunc))
}

private fun RBuilder.saveButton(onSave: () -> Unit) = couplingButton(supersize, green, styles["saveButton"], onSave) {
    +"Save!"
}

private fun RBuilder.cancelButton(onDelete: () -> Unit) = couplingButton(small, red, styles["deleteButton"], onDelete) {
    +"Cancel"
}
