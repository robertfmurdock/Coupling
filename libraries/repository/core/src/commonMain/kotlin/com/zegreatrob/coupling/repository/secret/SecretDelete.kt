package com.zegreatrob.coupling.repository.secret

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.SecretId

interface SecretDelete {

    suspend fun deleteSecret(partyId: PartyId, secretId: SecretId): Boolean
}
