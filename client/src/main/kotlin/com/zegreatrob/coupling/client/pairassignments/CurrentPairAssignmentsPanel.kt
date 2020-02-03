package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.AssignedPair.assignedPair
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div

data class CurrentPairAssignmentsPanelProps(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument?,
    val onPlayerSwap: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
    val onPinDrop: (Pin, PinnedCouplingPair) -> Unit,
    val onSave: () -> Unit,
    val pathSetter: (String) -> Unit
) : RProps

object CurrentPairAssignmentsPanel : FRComponent<CurrentPairAssignmentsPanelProps>(provider()) {

    private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

    fun RBuilder.currentPairAssignments(
        tribe: Tribe,
        pairAssignments: PairAssignmentDocument?,
        onPlayerSwap: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        onPinDrop: (Pin, PinnedCouplingPair) -> Unit,
        onSave: () -> Unit,
        pathSetter: (String) -> Unit
    ) = child(
        CurrentPairAssignmentsPanel.component.rFunction,
        CurrentPairAssignmentsPanelProps(tribe, pairAssignments, onPlayerSwap, onPinDrop, onSave, pathSetter)
    )

    override fun render(props: CurrentPairAssignmentsPanelProps) = with(props) {
        reactElement {
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
    }

    private fun RBuilder.noPairsHeader() = div(classes = styles["noPairsNotice"]) { +"No pair assignments yet!" }

    private fun RBuilder.dateHeader(pairAssignments: PairAssignmentDocument) = div {
        div {
            div(classes = styles["pairAssignmentsHeader"]) {
                +"Couples for ${pairAssignments.dateText()}"
            }
        }
    }

    private fun RBuilder.pairAssignmentList(
        tribe: Tribe,
        pairAssignments: PairAssignmentDocument,
        onPlayerSwap: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        onPinDrop: (Pin, PinnedCouplingPair) -> Unit,
        pathSetter: (String) -> Unit
    ) = div(classes = styles["pairAssignmentsContent"]) {
        pairAssignments.pairs.mapIndexed { index, pair ->
            assignedPair(tribe, pair, onPlayerSwap, onPinDrop, pairAssignments, pathSetter, key = "$index")
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

}
