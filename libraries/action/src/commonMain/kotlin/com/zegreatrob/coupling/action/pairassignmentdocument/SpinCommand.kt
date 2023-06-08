package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SpinCommand(val partyId: PartyId, val playerIds: List<String>, val pinIds: List<String>) :
    SimpleSuspendAction<SpinCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SpinCommand): VoidResult
    }
}
