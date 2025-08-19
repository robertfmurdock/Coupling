package com.zegreatrob.coupling.server.action

fun interface SecretValidator {
    suspend fun validateSubject(secret: String): String?
}
