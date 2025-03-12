package com.zegreatrob.coupling.action.secret

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class DeleteSecretCommand(val partyId: PartyId, val secretId: SecretId) {
    fun interface Dispatcher {
        suspend fun perform(command: DeleteSecretCommand): VoidResult
    }
}
