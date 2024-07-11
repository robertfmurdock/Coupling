package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ClearContributionsCommand(
    val partyId: PartyId,
) {
    fun interface Dispatcher {
        suspend fun perform(command: ClearContributionsCommand): VoidResult
    }
}
