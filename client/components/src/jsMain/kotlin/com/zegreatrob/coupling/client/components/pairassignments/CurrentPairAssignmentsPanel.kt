package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPath
import com.zegreatrob.coupling.client.components.green
import com.zegreatrob.coupling.client.components.red
import com.zegreatrob.coupling.client.components.small
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.useState
import web.cssom.ClassName
import web.cssom.px

external interface CurrentPairAssignmentsPanelProps : Props {
    var party: PartyDetails
    var pairAssignments: PairingSet
    var setPairAssignments: (PairingSet) -> Unit
    var allowSave: Boolean
    var dispatchFunc: DispatchFunc<DeletePairAssignmentsCommand.Dispatcher>
}

@ReactFunc
val CurrentPairAssignmentsPanel by nfc<CurrentPairAssignmentsPanelProps> { props ->
    val (party, pairAssignments, setPairAssignments, allowSave, dispatchFunc) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToCurrentFunc = { setRedirectUrl(party.id.currentPairsPath()) }
    val onCancel = dispatchFunc {
        fire(DeletePairAssignmentsCommand(party.id, pairAssignments.id))
        redirectToCurrentFunc()
    }
    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        div {
            className = ClassName("current-pair-assignments")
            dateHeader(pairAssignments)
            PairAssignmentBoard {
                this.party = party
                this.pairAssignments = pairAssignments
                this.setPairAssignments = setPairAssignments
                this.allowSave = allowSave
            }
            if (allowSave) {
                div {
                    saveButton(redirectToCurrentFunc)
                    cancelButton(onCancel)
                }
            }
        }
    }
}

private fun ChildrenBuilder.dateHeader(pairAssignments: PairingSet) = div {
    div { PairAssignmentsHeader(pairAssignments) }
}

private fun ChildrenBuilder.saveButton(onSave: () -> Unit) = CouplingButton {
    sizeRuleSet = supersize
    colorRuleSet = green
    onClick = onSave
    css { margin = 4.px }
    +"Save!"
}

private fun ChildrenBuilder.cancelButton(onCancel: () -> Unit) = CouplingButton {
    sizeRuleSet = small
    colorRuleSet = red
    onClick = onCancel
    +"Cancel"
}
