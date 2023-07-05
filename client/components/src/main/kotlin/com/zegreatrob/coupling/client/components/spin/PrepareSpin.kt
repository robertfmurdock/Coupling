package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.Navigate
import react.useState

external interface PrepareSpinProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var currentPairsDoc: PairAssignmentDocument?
    var pins: List<Pin>
    var dispatchFunc: DispatchFunc<out SpinCommand.Dispatcher>
}

@ReactFunc
val PrepareSpin by nfc<PrepareSpinProps> { (party, players, currentPairsDoc, pins, dispatchFunc) ->
    var playerSelections by useState(defaultSelections(players, currentPairsDoc))
    var pinSelections by useState(pins.map { it.id })
    var redirectUrl by useState<String?>(null)
    val onSpin = onSpin(dispatchFunc, party, playerSelections, pinSelections) { redirectUrl = it }
    val setPinSelections: (List<String?>) -> Unit = { pinSelections = it }
    val setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit = { playerSelections = it }
    if (redirectUrl != null) {
        Navigate { to = redirectUrl ?: "" }
    } else {
        PrepareSpinContent(
            party,
            playerSelections,
            pins,
            pinSelections,
            setPlayerSelections,
            setPinSelections,
            onSpin,
        )
    }
}

private fun defaultSelections(players: List<Player>, currentPairsDoc: PairAssignmentDocument?) = players.map { player ->
    player to isInLastSetOfPairs(player, currentPairsDoc)
}

private fun isInLastSetOfPairs(player: Player, currentPairsDoc: PairAssignmentDocument?) = currentPairsDoc
    ?.pairs
    ?.map(PinnedCouplingPair::players)
    ?.flatten()
    ?.map(Player::id)
    ?.contains(player.id)
    ?: false

private fun List<Pair<Player, Boolean>>.playerIds() = filter { (_, isSelected) -> isSelected }.map { it.first.id }

private fun onSpin(
    dispatchFunc: DispatchFunc<out SpinCommand.Dispatcher>,
    party: PartyDetails,
    playerSelections: List<Pair<Player, Boolean>>,
    pinSelections: List<String?>,
    setRedirectUrl: (String) -> Unit,
) = dispatchFunc(
    { SpinCommand(party.id, playerSelections.playerIds(), pinSelections.filterNotNull()) },
    { setRedirectUrl(party.newPairAssignmentsPath()) },
)
