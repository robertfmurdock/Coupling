package com.zegreatrob.coupling.components.spin

import com.zegreatrob.coupling.components.DispatchFunc
import com.zegreatrob.coupling.components.NewPairAssignmentsCommand
import com.zegreatrob.coupling.components.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.components.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.router.Navigate
import react.useState

data class PrepareSpin(
    val party: Party,
    val players: List<Player>,
    val currentPairsDoc: PairAssignmentDocument?,
    val pins: List<Pin>,
    val dispatchFunc: DispatchFunc<out NewPairAssignmentsCommandDispatcher>
) : DataPropsBind<PrepareSpin>(prepareSpin)

val prepareSpin = tmFC<PrepareSpin> { (party, players, currentPairsDoc, pins, dispatchFunc) ->
    var playerSelections by useState(defaultSelections(players, currentPairsDoc))
    var pinSelections by useState(pins.map { it.id })
    var redirectUrl by useState<String?>(null)
    val onSpin = onSpin(dispatchFunc, party, playerSelections, pinSelections) { redirectUrl = it }

    if (redirectUrl != null) {
        Navigate { to = redirectUrl ?: "" }
    } else {
        add(
            PrepareSpinContent(
                party,
                playerSelections,
                pins,
                pinSelections,
                { playerSelections = it },
                { pinSelections = it },
                onSpin
            )
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
    dispatchFunc: DispatchFunc<out NewPairAssignmentsCommandDispatcher>,
    party: Party,
    playerSelections: List<Pair<Player, Boolean>>,
    pinSelections: List<String?>,
    setRedirectUrl: (String) -> Unit
) = dispatchFunc(
    { NewPairAssignmentsCommand(party.id, playerSelections.playerIds(), pinSelections.filterNotNull()) },
    { setRedirectUrl(party.newPairAssignmentsPath()) }
)
