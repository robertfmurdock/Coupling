package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SaveBoostCommand(val partyIds: Set<PartyId>) {
    interface Dispatcher {
        suspend fun perform(command: SaveBoostCommand): Result
    }

    sealed interface Result {
        data object Success : Result
        data class Unknown(val message: String) : Result
    }
}
