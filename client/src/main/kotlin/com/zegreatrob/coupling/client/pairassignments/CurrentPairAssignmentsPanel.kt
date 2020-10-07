package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.green
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
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
    onSave: () -> Unit,
    pathSetter: (String) -> Unit
) = child(
    CurrentPairAssignmentsPanel,
    CurrentPairAssignmentsPanelProps(tribe, pairAssignments, onPlayerSwap, onPinDrop, allowSave, onSave, pathSetter)
)

data class CurrentPairAssignmentsPanelProps(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument,
    val onPlayerSwap: SwapCallback,
    val onPinDrop: PinMoveCallback,
    val allowSave: Boolean,
    val onSave: () -> Unit,
    val pathSetter: (String) -> Unit
) : RProps

private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

val CurrentPairAssignmentsPanel = reactFunction<CurrentPairAssignmentsPanelProps> { props ->
    val (tribe, pairAssignments, onPlayerSwap, onPinDrop, allowSave, onSave, pathSetter) = props
    div(classes = styles.className) {
        dateHeader(pairAssignments)
        pairAssignmentList(tribe, pairAssignments, onPlayerSwap, onPinDrop, pathSetter)
        saveButtonSection(onSave, allowSave)
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
    pathSetter: (String) -> Unit
) = div(classes = styles["pairAssignmentsContent"]) {
    pairAssignments.pairs.mapIndexed { index, pair ->
        assignedPair(tribe, pair, onPlayerSwap, onPinDrop, pairAssignments.isNotSaved(), pathSetter, key = "$index")
    }
}

private fun RBuilder.saveButtonSection(onSave: () -> Unit, allowSave: Boolean) = div {
    if (allowSave) {
        saveButton(onSave)
    }
}

private fun PairAssignmentDocument.isNotSaved() = id == null

private fun RBuilder.saveButton(onSave: () -> Unit) = couplingButton(supersize, green, styles["saveButton"], onSave) {
    +"Save!"
}
