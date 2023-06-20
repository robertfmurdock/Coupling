package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import react.router.Navigate
import react.useState

data class PrepareSpin(
    val party: PartyDetails,
    val players: List<Player>,
    val currentPairsDoc: PairAssignmentDocument?,
    val pins: List<Pin>,
    val dispatchFunc: DispatchFunc<out SpinCommand.Dispatcher>,
) : DataPropsBind<PrepareSpin>(prepareSpin)

val prepareSpin by ntmFC<PrepareSpin> { (party, players, currentPairsDoc, pins, dispatchFunc) ->
    var playerSelections by useState(defaultSelections(players, currentPairsDoc))
    var pinSelections by useState(pins.map { it.id })
    var redirectUrl by useState<String?>(null)
    val onSpin = onSpin(dispatchFunc, party, playerSelections, pinSelections) { redirectUrl = it }
    val setPinSelections: (List<String?>) -> Unit = { pinSelections = it }
    val setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit = { playerSelections = it }
    if (redirectUrl != null) {
        Navigate { to = redirectUrl ?: "" }
    } else {
        add(
            PrepareSpinContent(
                party,
                playerSelections,
                pins,
                pinSelections,
                setPlayerSelections,
                setPinSelections,
                onSpin,
            ),
        )
    }
}

private fun defaultSelections(players: List<Player>, currentPairsDoc: PairAssignmentDocument?) = players.map { player ->
    player to isInLastSetOfPairs(player, currentPairsDoc)
}

private fun isInLastSetOfPairs(player: Player, currentPairsDoc: PairAssignmentDocument?) = currentPairsDoc
    ?.pairs
    ?.map { it.players }
    ?.flatten()
    ?.map { it.player.id }
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
