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
    val swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
    val pinDropCallback: (Pin, PinnedCouplingPair) -> Unit,
    val onSave: () -> Unit,
    val pathSetter: (String) -> Unit
) : RProps

object CurrentPairAssignmentsPanel : FRComponent<CurrentPairAssignmentsPanelProps>(provider()) {

    private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

    fun RBuilder.currentPairAssignments(
        tribe: Tribe,
        pairAssignments: PairAssignmentDocument?,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pinDropCallback: (Pin, PinnedCouplingPair) -> Unit,
        onSave: () -> Unit,
        pathSetter: (String) -> Unit
    ) = child(
        CurrentPairAssignmentsPanel.component.rFunction,
        CurrentPairAssignmentsPanelProps(tribe, pairAssignments, swapCallback, pinDropCallback, onSave, pathSetter)
    )

    override fun render(props: CurrentPairAssignmentsPanelProps) = reactElement {
        val (tribe, pairAssignments, onSwap, onPinDrop, onSave, pathSetter) = props
        div(classes = styles.className) {
            pairAssignmentsHeader(pairAssignments)
            pairAssignmentList(pairAssignments, onSwap, onPinDrop, tribe, pathSetter)
            saveButtonSection(pairAssignments, onSave)
        }
    }

    private fun RBuilder.pairAssignmentsHeader(pairAssignments: PairAssignmentDocument?) =
        if (pairAssignments != null) {
            div {
                div {
                    div(classes = styles["pairAssignmentsHeader"]) {
                        +"Couples for ${pairAssignments.dateText()}"
                    }
                }
            }
        } else {
            div(classes = styles["noPairsNotice"]) { +"No pair assignments yet!" }
        }

    private fun RBuilder.pairAssignmentList(
        pairAssignments: PairAssignmentDocument?,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pinDropCallback: (Pin, PinnedCouplingPair) -> Unit,
        tribe: Tribe,
        pathSetter: (String) -> Unit
    ) = div(classes = styles["pairAssignmentsContent"]) {
        pairAssignments?.pairs?.mapIndexed { index, pair ->
            assignedPair(tribe, pair, swapCallback, pinDropCallback, pairAssignments, pathSetter, key = "$index")
        }
    }


    private fun RBuilder.saveButtonSection(pairAssignments: PairAssignmentDocument?, onSave: () -> Unit) = div {
        if (pairAssignments != null && pairAssignments.id == null) {
            saveButton(onSave)
        }
    }

    private fun RBuilder.saveButton(onSave: () -> Unit) = a(classes = "super green button") {
        attrs {
            classes += styles["saveButton"]
            onClickFunction = { onSave() }
        }
        +"Save!"
    }

}
