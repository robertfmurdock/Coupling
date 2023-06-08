package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SpinAction(
    val party: Party,
    val players: List<Player>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
) : SimpleSuspendAction<SpinAction.Dispatcher, PairAssignmentDocument?> {
    override val performFunc = link(Dispatcher::perform)

    fun interface Dispatcher {
        suspend fun perform(action: SpinAction): PairAssignmentDocument?
    }
}
