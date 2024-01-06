package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.server.action.SecretGenerator
import com.zegreatrob.coupling.server.secret.external.jose.SignJWT
import js.objects.jso
import js.typedarrays.Uint8Array
import kotlinx.coroutines.await

interface JwtSecretGenerator : SecretGenerator {

    val secretIssuer: String
    val secretSigningSecret: String
    val secretAudience: String

    override suspend fun createSecret(secret: PartyElement<Secret>) = SignJWT(customClaims(secret))
        .setAudience(secretAudience)
        .setSubject(secret.partyId.value)
        .setIssuedAt()
        .setIssuer(secretIssuer)
        .setProtectedHeader(jso { alg = "HS256" })
        .sign(TextEncoder().encode(secretSigningSecret))
        .await()

    private fun customClaims(secret: PartyElement<Secret>): dynamic = jso {
        this["https://zegreatrob.com/secret-id"] = secret.element.id
    }
}

external class TextEncoder {
    fun encode(input: String = definedExternally): Uint8Array
}
