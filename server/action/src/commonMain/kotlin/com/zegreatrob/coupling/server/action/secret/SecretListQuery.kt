package com.zegreatrob.coupling.server.action.secret

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.secret.SecretListGet
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SecretListQuery(val partyId: PartyId) {
    interface Dispatcher {
        val secretRepository: SecretListGet
        suspend fun perform(query: SecretListQuery) = query.partyId.let { secretRepository.getSecrets(it) }
    }
}
