package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.party.SecretId

fun interface SecretValidator {
    suspend fun validateSubject(secret: String): Pair<SecretId, String>?
}
