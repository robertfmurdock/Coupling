package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SaveSlackIntegrationCommand(
    val partyId: PartyId,
    val channel: String,
    val team: String,
) {
    fun interface Dispatcher {
        suspend fun perform(command: SaveSlackIntegrationCommand): VoidResult
    }
}
