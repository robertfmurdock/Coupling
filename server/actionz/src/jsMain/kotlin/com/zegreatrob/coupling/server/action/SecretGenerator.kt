package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.user.UserId

fun interface PartySecretGenerator {
    suspend fun createSecret(secret: PartyElement<Secret>): String
}

fun interface UserSecretGenerator {
    suspend fun createSecret(secret: Pair<UserId, SecretId>): String
}
