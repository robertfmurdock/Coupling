package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.client.reactFunction
import react.Props
import react.RBuilder
import react.dom.div
import react.router.Navigate
import react.useState

fun RBuilder.currentPairAssignments(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>
) = child(
    CurrentPairAssignmentsPanel,
    CurrentPairAssignmentsPanelProps(tribe, pairAssignments, setPairAssignments, allowSave, dispatchFunc)
)

data class CurrentPairAssignmentsPanelProps(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument,
    val setPairAssignments: (PairAssignmentDocument) -> Unit,
    val allowSave: Boolean,
    val dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>
) : Props

private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

val CurrentPairAssignmentsPanel = reactFunction<CurrentPairAssignmentsPanelProps> { props ->
    val (tribe, pairAssignments, setPairAssignments, allowSave, dispatchFunc) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToCurrentFunc = { setRedirectUrl(tribe.id.currentPairsPage()) }
    val onCancel = dispatchFunc(
        { DeletePairAssignmentsCommand(tribe.id, pairAssignments.id) }, { redirectToCurrentFunc() }
    )
    if (redirectUrl != null)
        Navigate { attrs.to = redirectUrl }
    else
        div(classes = styles.className) {
            dateHeader(pairAssignments)
            pairAssignmentList(tribe, pairAssignments, setPairAssignments, allowSave)
            if (allowSave) {
                div {
//                    prompt(`when` = true, message = "Press OK to save these pairs.")
                    saveButton(redirectToCurrentFunc)
                    cancelButton(onCancel)
                }
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
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean
) = div(classes = styles["pairAssignmentsContent"]) {
    pairAssignments.pairs.mapIndexed { index, pair ->
        assignedPair(
            tribe,
            pair,
            swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: String ->
                setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
            },
            dropPinFunc = { pinId -> setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, pair)) },
            canDrag = allowSave,
            key = "$index"
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
    targetPair: PinnedCouplingPair
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
        }
    )
}

private fun PinnedCouplingPair.replacePlayer(playerToReplace: PinnedPlayer, replacement: PinnedPlayer) =
    copy(players = players.map { pinnedPlayer ->
        if (pinnedPlayer == playerToReplace) {
            replacement
        } else {
            pinnedPlayer
        }
    })

private fun List<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: String) = firstOrNull { pair ->
    pair.players.any { player -> player.player.id == droppedPlayerId }
}


private fun RBuilder.saveButton(onSave: () -> Unit) = couplingButton(supersize, green, styles["saveButton"], onSave) {
    +"Save!"
}

private fun RBuilder.cancelButton(onCancel: () -> Unit) = couplingButton(small, red, styles["deleteButton"], onCancel) {
    +"Cancel"
}
