package com.zegreatrob.coupling.repository.secret

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret

interface SecretListGet {

    suspend fun getSecrets(partyId: PartyId): List<PartyRecord<Secret>>
}
