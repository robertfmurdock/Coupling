package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPage
import com.zegreatrob.coupling.client.components.green
import com.zegreatrob.coupling.client.components.red
import com.zegreatrob.coupling.client.components.small
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.useState
import web.cssom.ClassName
import web.cssom.WhiteSpace
import web.cssom.px

external interface CurrentPairAssignmentsPanelProps : Props {
    var party: PartyDetails
    var pairAssignments: PairAssignmentDocument
    var setPairAssignments: (PairAssignmentDocument) -> Unit
    var allowSave: Boolean
    var dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommand.Dispatcher>
}

@ReactFunc
val CurrentPairAssignmentsPanel by nfc<CurrentPairAssignmentsPanelProps> { props ->
    val (party, pairAssignments, setPairAssignments, allowSave, dispatchFunc) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToCurrentFunc = { setRedirectUrl(party.id.currentPairsPage()) }
    val onCancel = dispatchFunc(
        commandFunc = { DeletePairAssignmentsCommand(party.id, pairAssignments.id) },
        fireFunc = ::perform,
        response = { redirectToCurrentFunc() },
    )
    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        div {
            className = ClassName("current-pair-assignments")
            dateHeader(pairAssignments)
            pairAssignmentList(party, pairAssignments, setPairAssignments, allowSave)
            if (allowSave) {
                div {
                    saveButton(redirectToCurrentFunc)
                    cancelButton(onCancel)
                }
            }
        }
    }
}

private fun ChildrenBuilder.dateHeader(pairAssignments: PairAssignmentDocument) =
    div { div { PairAssignmentsHeader(pairAssignments) } }

private fun ChildrenBuilder.pairAssignmentList(
    party: PartyDetails,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
) = div {
    css {
        whiteSpace = WhiteSpace.preLine
    }
    pairAssignments.pairs.mapIndexed { index, pair ->
        AssignedPair(
            party = party,
            pair = pair,
            canDrag = allowSave,
            swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: String ->
                setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
            },
            pinDropFunc = { pinId: String -> setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, pair)) },
            key = "$index",
        )
    }
}

private fun PairAssignmentDocument.copyWithDroppedPin(pinId: String, pair: PinnedCouplingPair) =
    copy(pairs = pairs.movePinTo(findDroppedPin(pinId, this), pair))

private fun findDroppedPin(id: String, pairAssignments: PairAssignmentDocument) = pairAssignments
    .pairs
    .map(PinnedCouplingPair::pins)
    .flatten()
    .first { it.id == id }

private fun List<PinnedCouplingPair>.movePinTo(pin: Pin, droppedPair: PinnedCouplingPair) = map { pair ->
    when {
        pair == droppedPair -> pair.addPin(pin)
        pair.pins.contains(pin) -> pair.removePin(pin)
        else -> pair
    }
}

private fun PinnedCouplingPair.addPin(pin: Pin) = copy(pins = pins + pin)

private fun PinnedCouplingPair.removePin(pin: Pin) = copy(pins = pins - pin)

private fun PairAssignmentDocument.copyWithSwappedPlayers(
    droppedPlayerId: String,
    targetPlayer: PinnedPlayer,
    targetPair: PinnedCouplingPair,
): PairAssignmentDocument {
    val sourcePair = pairs.findPairContainingPlayer(droppedPlayerId)
    val droppedPlayer = sourcePair?.pinnedPlayers?.firstOrNull { it.player.id == droppedPlayerId }

    if (sourcePair == targetPair || droppedPlayer == null) {
        return this
    }

    return copy(
        pairs = pairs.map { pair ->
            when (pair) {
                targetPair -> pair.replacePlayer(targetPlayer, droppedPlayer)
                sourcePair -> pair.replacePlayer(droppedPlayer, targetPlayer)
                else -> pair
            }
        },
    )
}

private fun PinnedCouplingPair.replacePlayer(playerToReplace: PinnedPlayer, replacement: PinnedPlayer) =
    copy(
        pinnedPlayers = pinnedPlayers.map { pinnedPlayer ->
            if (pinnedPlayer == playerToReplace) {
                replacement
            } else {
                pinnedPlayer
            }
        },
    )

private fun List<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: String) = firstOrNull { pair ->
    pair.pinnedPlayers.any { player -> player.player.id == droppedPlayerId }
}

private fun ChildrenBuilder.saveButton(onSave: () -> Unit) = CouplingButton(
    sizeRuleSet = supersize,
    colorRuleSet = green,
    onClick = onSave,
    css = fun PropertiesBuilder.() { margin = 4.px },
) {
    +"Save!"
}

private fun ChildrenBuilder.cancelButton(onCancel: () -> Unit) = CouplingButton(
    sizeRuleSet = small,
    colorRuleSet = red,
    onClick = onCancel,
) {
    +"Cancel"
}
