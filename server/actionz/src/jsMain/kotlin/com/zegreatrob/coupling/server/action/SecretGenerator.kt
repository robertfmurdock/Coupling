package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret

fun interface SecretGenerator {
    suspend fun createSecret(secret: PartyElement<Secret>): String
}
