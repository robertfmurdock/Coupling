package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
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
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.WhiteSpace
import csstype.px
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.useState

data class CurrentPairAssignmentsPanel(
    val party: Party,
    val pairAssignments: PairAssignmentDocument,
    val setPairAssignments: (PairAssignmentDocument) -> Unit,
    val allowSave: Boolean,
    val dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommand.Dispatcher>,
) : DataPropsBind<CurrentPairAssignmentsPanel>(currentPairAssignmentsPanel)

val currentPairAssignmentsPanel = tmFC<CurrentPairAssignmentsPanel> { props ->
    val (party, pairAssignments, setPairAssignments, allowSave, dispatchFunc) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToCurrentFunc = { setRedirectUrl(party.id.currentPairsPage()) }
    val onCancel = dispatchFunc(
        { DeletePairAssignmentsCommand(party.id, pairAssignments.id) },
        { redirectToCurrentFunc() },
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
//                    prompt(`when` = true, message = "Press OK to save these pairs.")
                    saveButton(redirectToCurrentFunc)
                    cancelButton(onCancel)
                }
            }
        }
    }
}

private fun ChildrenBuilder.dateHeader(pairAssignments: PairAssignmentDocument) = div {
    div {
        add(PairAssignmentsHeader(pairAssignments))
    }
}

private fun ChildrenBuilder.pairAssignmentList(
    party: Party,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
) = div {
    css {
        whiteSpace = WhiteSpace.preLine
    }
    pairAssignments.pairs.mapIndexed { index, pair ->
        add(
            AssignedPair(
                party,
                pair,
                canDrag = allowSave,
                swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: String ->
                    setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
                },
                pinDropFunc = { pinId: String -> setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, pair)) },
            ),
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
    val droppedPlayer = sourcePair?.players?.firstOrNull { it.player.id == droppedPlayerId }

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
        players = players.map { pinnedPlayer ->
            if (pinnedPlayer == playerToReplace) {
                replacement
            } else {
                pinnedPlayer
            }
        },
    )

private fun List<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: String) = firstOrNull { pair ->
    pair.players.any { player -> player.player.id == droppedPlayerId }
}

private fun ChildrenBuilder.saveButton(onSave: () -> Unit) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(
        sizeRuleSet = com.zegreatrob.coupling.client.components.supersize,
        colorRuleSet = com.zegreatrob.coupling.client.components.green,
        onClick = onSave,
        css = { margin = 4.px },
    ),
) {
    +"Save!"
}

private fun ChildrenBuilder.cancelButton(onCancel: () -> Unit) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(
        sizeRuleSet = com.zegreatrob.coupling.client.components.small,
        colorRuleSet = com.zegreatrob.coupling.client.components.red,
        onClick = onCancel,
    ),
) {
    +"Cancel"
}
