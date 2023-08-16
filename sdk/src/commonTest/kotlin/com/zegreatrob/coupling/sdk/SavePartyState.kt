package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun savePartyState(
    party: PartyDetails,
    players: List<Player>,
    pairAssignmentDocs: List<PairAssignmentDocument>,
) = coroutineScope {
    with(sdk()) {
        fire(SavePartyCommand(party))
        launch {
            players.forEach {
                fire(SavePlayerCommand(party.id, it))
            }
        }
        launch {
            pairAssignmentDocs.forEach {
                fire(SavePairAssignmentsCommand(party.id, it))
            }
        }
    }
}
