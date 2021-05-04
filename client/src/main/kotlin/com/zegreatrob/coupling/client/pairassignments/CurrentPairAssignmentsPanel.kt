package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RProps
import react.dom.div
import react.router.dom.redirect
import react.useState

fun RBuilder.currentPairAssignments(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>
) = child(
    CurrentPairAssignmentsPanel,
    CurrentPairAssignmentsPanelProps(tribe, pairAssignments, setPairAssignments, allowSave, controls)
)

data class CurrentPairAssignmentsPanelProps(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument,
    val setPairAssignments: (PairAssignmentDocument) -> Unit,
    val allowSave: Boolean,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>
) : RProps

private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

val CurrentPairAssignmentsPanel = reactFunction<CurrentPairAssignmentsPanelProps> { props ->
    val (tribe, pairAssignments, setPairAssignments, allowSave, controls) = props
    div(classes = styles.className) {
        dateHeader(pairAssignments)
        pairAssignmentList(tribe, pairAssignments, setPairAssignments, allowSave, controls.pathSetter)
        if (allowSave) {
            controlSection(tribe, pairAssignments, controls.dispatchFunc)
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
    allowSave: Boolean,
    pathSetter: (String) -> Unit
) = div(classes = styles["pairAssignmentsContent"]) {
    pairAssignments.pairs.mapIndexed { index, pair ->
        assignedPair(
            tribe,
            pair,
            key = "$index",
            canDrag = allowSave,
            swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: String ->
                setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
            },
            dropPinFunc = { pinId -> setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, pair)) },
            pathSetter = pathSetter
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


private fun RBuilder.controlSection(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>
) = div {
    val (shouldPrompt, setShouldPrompt) = useState(true)
    promptOnExit(shouldPrompt)
    if(!shouldPrompt) {
        redirect(to = tribe.id.currentPairsPage())
    }
    val redirectToCurrentFunc = { setShouldPrompt(false) }
    saveButton(redirectToCurrentFunc)
    cancelButton(dispatchFunc({ DeletePairAssignmentsCommand(tribe.id, pairAssignments.id) }, { redirectToCurrentFunc() }))
}

private fun RBuilder.promptOnExit(shouldShowPrompt: Boolean) = prompt(
    `when` = shouldShowPrompt,
    message = "Press OK to save these pairs."
)

private fun RBuilder.saveButton(onSave: () -> Unit) = couplingButton(supersize, green, styles["saveButton"], onSave) {
    +"Save!"
}

private fun RBuilder.cancelButton(onDelete: () -> Unit) = couplingButton(small, red, styles["deleteButton"], onDelete) {
    +"Cancel"
}
