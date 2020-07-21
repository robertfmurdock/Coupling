package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div

data class CurrentPairAssignmentsPanelProps(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument?,
    val onPlayerSwap: SwapCallback,
    val onPinDrop: PinMoveCallback,
    val onSave: () -> Unit,
    val pathSetter: (String) -> Unit
) : RProps

private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

val CurrentPairAssignmentsPanel =
    reactFunction<CurrentPairAssignmentsPanelProps> { props ->
        val (tribe, pairAssignments, onPlayerSwap, onPinDrop, onSave, pathSetter) = props
        div(classes = styles.className) {
            if (pairAssignments == null) {
                noPairsHeader()
            } else {
                dateHeader(pairAssignments)
                pairAssignmentList(tribe, pairAssignments, onPlayerSwap, onPinDrop, pathSetter)
                saveButtonSection(pairAssignments, onSave)
            }
        }
    }

private fun RBuilder.noPairsHeader() = div(classes = styles["noPairsNotice"]) { +"No pair assignments yet!" }

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

private fun RBuilder.saveButtonSection(pairAssignments: PairAssignmentDocument, onSave: () -> Unit) = div {
    if (pairAssignments.isNotSaved()) {
        saveButton(onSave)
    }
}

private fun PairAssignmentDocument.isNotSaved() = id == null

private fun RBuilder.saveButton(onSave: () -> Unit) = a(classes = "super green button") {
    attrs {
        classes += styles["saveButton"]
        onClickFunction = { onSave() }
    }
    +"Save!"
}

fun RBuilder.currentPairAssignments(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument?,
    onPlayerSwap: SwapCallback,
    onPinDrop: PinMoveCallback,
    onSave: () -> Unit,
    pathSetter: (String) -> Unit
) = child(
    CurrentPairAssignmentsPanel,
    CurrentPairAssignmentsPanelProps(tribe, pairAssignments, onPlayerSwap, onPinDrop, onSave, pathSetter)
)
