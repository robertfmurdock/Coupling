package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.secret.external.jose.SignJWT
import js.core.jso
import js.typedarrays.Uint8Array
import kotlinx.coroutines.await

interface SecretGenerator {

    val secretIssuer: String
    val secretSigningSecret: String
    val secretAudience: String

    suspend fun createSecret(partyId: PartyId, secretId: String) = SignJWT(jso {})
        .setAudience(secretAudience)
        .setSubject(partyId.value)
        .setIssuedAt()
        .setIssuer(secretIssuer)
        .setProtectedHeader(jso { alg = "HS256" })
        .sign(TextEncoder().encode(secretSigningSecret))
        .await()
}

external class TextEncoder {
    fun encode(input: String = definedExternally): Uint8Array
}
