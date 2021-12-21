package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.child
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
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.RBuilder
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.useState

fun RBuilder.currentPairAssignments(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>
) = child(
    CurrentPairAssignmentsPanel(tribe, pairAssignments, setPairAssignments, allowSave, dispatchFunc)
)

data class CurrentPairAssignmentsPanel(
    val tribe: Tribe,
    val pairAssignments: PairAssignmentDocument,
    val setPairAssignments: (PairAssignmentDocument) -> Unit,
    val allowSave: Boolean,
    val dispatchFunc: DispatchFunc<out DeletePairAssignmentsCommandDispatcher>
) : DataProps<CurrentPairAssignmentsPanel> {
    override val component: TMFC<CurrentPairAssignmentsPanel> get() = currentPairAssignmentsPanel
}

private val styles = useStyles("pairassignments/CurrentPairAssignmentsPanel")

val currentPairAssignmentsPanel = tmFC<CurrentPairAssignmentsPanel> { props ->
    val (tribe, pairAssignments, setPairAssignments, allowSave, dispatchFunc) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToCurrentFunc = { setRedirectUrl(tribe.id.currentPairsPage()) }
    val onCancel = dispatchFunc(
        { DeletePairAssignmentsCommand(tribe.id, pairAssignments.id) }, { redirectToCurrentFunc() }
    )
    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else
        div {
            className = styles.className
            dateHeader(pairAssignments)
            pairAssignmentList(tribe, pairAssignments, setPairAssignments, allowSave)
            if (allowSave) {
                div {
//                    prompt(`when` = true, message = "Press OK to save these pairs.")
                    child(saveButton(redirectToCurrentFunc))
                    child(cancelButton(onCancel))
                }
            }
        }
}

private fun ChildrenBuilder.dateHeader(pairAssignments: PairAssignmentDocument) = div {
    div {
        child(PairAssignmentsHeader(pairAssignments))
    }
}

private fun ChildrenBuilder.pairAssignmentList(
    tribe: Tribe,
    pairAssignments: PairAssignmentDocument,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean
) = div {
    className = styles["pairAssignmentsContent"]
    pairAssignments.pairs.mapIndexed { index, pair ->
        child(
            key = "$index",
            dataProps = AssignedPair(
                tribe,
                pair,
                canDrag = allowSave,
                swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: String ->
                    setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
                },
                pinDropFunc = { pinId: String -> setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, pair)) }
            )
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


private fun saveButton(onSave: () -> Unit): CouplingButton =
    CouplingButton(supersize, green, styles["saveButton"], onSave, {}) { +"Save!" }

private fun cancelButton(onCancel: () -> Unit) =
    CouplingButton(small, red, styles["deleteButton"], onCancel, {}, { +"Cancel" })
