package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SpinCommand(val partyId: PartyId, val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendAction<SpinCommand.Dispatcher, PairAssignmentDocument> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(action: SpinCommand): PairAssignmentDocument
    }
}
