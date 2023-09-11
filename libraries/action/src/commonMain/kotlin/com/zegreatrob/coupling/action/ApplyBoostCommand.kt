package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ApplyBoostCommand(val partyId: PartyId) {
    fun interface Dispatcher {
        suspend fun perform(command: ApplyBoostCommand): Result
    }

    sealed interface Result {
        data object Success : Result
        data class Unknown(val message: String) : Result
    }
}
