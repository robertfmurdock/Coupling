package com.zegreatrob.coupling.action.secret

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class CreateSecretCommand(val partyId: PartyId, val description: String) {
    fun interface Dispatcher {
        suspend fun perform(command: CreateSecretCommand): Pair<Secret, String>?
    }
}
