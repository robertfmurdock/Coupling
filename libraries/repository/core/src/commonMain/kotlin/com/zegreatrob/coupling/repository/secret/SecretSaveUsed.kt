package com.zegreatrob.coupling.repository.secret

import com.zegreatrob.coupling.model.party.SecretUsed

fun interface SecretSaveUsed {
    suspend fun save(used: SecretUsed)
}
