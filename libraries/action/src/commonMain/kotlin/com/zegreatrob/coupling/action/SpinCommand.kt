package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class SpinCommand(val partyId: PartyId, val playerIds: NotEmptyList<String>, val pinIds: List<String>) {
    interface Dispatcher {
        suspend fun perform(command: SpinCommand): Result
    }

    sealed interface Result {
        data object Success : Result
        data class PartyDoesNotExist(val partyId: PartyId) : Result
        data class CouldNotFindPlayers(val missingPlayerIds: NotEmptyList<String>) : Result
    }
}
