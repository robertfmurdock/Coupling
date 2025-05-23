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
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotools.types.collection.NotEmptyList
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

private fun ChildrenBuilder.dateHeader(pairAssignments: PairAssignmentDocument) = div {
    div { PairAssignmentsHeader(pairAssignments) }
}

private fun ChildrenBuilder.pairAssignmentList(
    party: PartyDetails,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
) = div {
    css {
        whiteSpace = WhiteSpace.preLine
    }
    pairAssignments.pairs.toList().mapIndexed { index, pair ->
        AssignedPair(
            party = party,
            pair = pair,
            canDrag = allowSave,
            swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: PlayerId ->
                setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
            },
            pinDropFunc = { pinId: PinId, targetId: String ->
                setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, targetId))
            },
            key = "$index",
        )
    }
}

private fun PairAssignmentDocument.copyWithDroppedPin(pinId: PinId, pairId: String): PairAssignmentDocument {
    val droppedPair = pairs.toList().find { it.toPair().pairId == pairId }
    return if (droppedPair != null) {
        copy(pairs = pairs.movePinTo(findDroppedPin(pinId, this), droppedPair))
    } else {
        this
    }
}

private fun findDroppedPin(id: PinId, pairAssignments: PairAssignmentDocument) = pairAssignments
    .pairs
    .toList()
    .map(PinnedCouplingPair::pins)
    .flatten()
    .first { it.id == id }

private fun NotEmptyList<PinnedCouplingPair>.movePinTo(pin: Pin, droppedPair: PinnedCouplingPair) = map { pair ->
    when {
        pair == droppedPair -> pair.addPin(pin)
        pair.pins.contains(pin) -> pair.removePin(pin)
        else -> pair
    }
}

private fun PinnedCouplingPair.addPin(pin: Pin) = copy(pins = pins + pin)

private fun PinnedCouplingPair.removePin(pin: Pin) = copy(pins = pins - pin)

private fun PairAssignmentDocument.copyWithSwappedPlayers(
    droppedPlayerId: PlayerId,
    targetPlayer: PinnedPlayer,
    targetPair: PinnedCouplingPair,
): PairAssignmentDocument {
    val sourcePair = pairs.findPairContainingPlayer(droppedPlayerId)
    val droppedPlayer = sourcePair?.pinnedPlayers?.toList()?.firstOrNull { it.player.id == droppedPlayerId }

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

private fun PinnedCouplingPair.replacePlayer(playerToReplace: PinnedPlayer, replacement: PinnedPlayer) = copy(
    pinnedPlayers = pinnedPlayers.map { pinnedPlayer ->
        if (pinnedPlayer == playerToReplace) {
            replacement
        } else {
            pinnedPlayer
        }
    },
)

private fun NotEmptyList<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: PlayerId) = toList()
    .firstOrNull { pair ->
        pair.pinnedPlayers.toList().any { player -> player.player.id == droppedPlayerId }
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
