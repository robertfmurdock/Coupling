package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun savePartyState(
    party: PartyDetails,
    players: List<Player>,
    pairAssignmentDocs: List<PairingSet>,
) = coroutineScope {
    with(sdk()) {
        fire(SavePartyCommand(party))
        launch {
            fire(SavePartyCommand(partyId = party.id, players = players))
        }
        pairAssignmentDocs.forEach {
            launch {
                fire(SavePairAssignmentsCommand(party.id, it))
            }
        }
    }
}

suspend fun savePartyStateWithFixedPlayerOrder(
    party: PartyDetails,
    players: List<Player>,
    pairAssignmentDocs: List<PairingSet>,
) = coroutineScope {
    with(sdk()) {
        fire(SavePartyCommand(party))
        launch {
            fire(SavePartyCommand(partyId = party.id, players = players))
        }
        pairAssignmentDocs.forEach {
            launch {
                fire(SavePairAssignmentsCommand(party.id, it))
            }
        }
    }
}
