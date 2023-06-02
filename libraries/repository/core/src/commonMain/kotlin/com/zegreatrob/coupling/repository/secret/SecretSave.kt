package com.zegreatrob.coupling.repository.secret

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret

interface SecretSave {
    suspend fun save(it: PartyElement<Secret>)
}
