package com.zegreatrob.coupling.repository.secret

import com.zegreatrob.coupling.model.party.PartyId

interface SecretDelete {

    suspend fun deleteSecret(partyId: PartyId, secretId: String): Boolean
}
