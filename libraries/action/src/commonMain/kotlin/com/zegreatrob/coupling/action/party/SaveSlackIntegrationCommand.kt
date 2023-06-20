package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SaveSlackIntegrationCommand(
    val partyId: PartyId,
    val channel: String,
    val team: String,
) :
    SimpleSuspendAction<SaveSlackIntegrationCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SaveSlackIntegrationCommand): VoidResult
    }
}
