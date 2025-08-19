package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.server.action.SecretGenerator
import com.zegreatrob.coupling.server.secret.external.jose.SignJWT
import js.objects.Record
import js.objects.recordOf
import js.objects.unsafeJso
import js.typedarrays.Uint8Array
import kotlinx.coroutines.await
import kotools.types.text.NotBlankString

interface JwtSecretGenerator : SecretGenerator {

    val secretIssuer: String
    val secretSigningSecret: String
    val secretAudience: String

    override suspend fun createSecret(secret: PartyElement<Secret>) = jwt(
        subject = secret.partyId.value,
        secretId = secret.element.id,
        exp = null,
    )

    override suspend fun createSecret(secret: Pair<UserId, SecretId>): String = jwt(
        secret.first.value,
        secret.second,
        "2h",
    )

    private suspend fun jwt(subject: NotBlankString, secretId: SecretId, exp: String?): String = SignJWT(customClaims(secretId))
        .setAudience(secretAudience)
        .setSubject(subject.toString())
        .setIssuedAt()
        .also { if (exp != null) it.setExpirationTime(exp) }
        .setIssuer(secretIssuer)
        .setProtectedHeader(unsafeJso { alg = "HS256" })
        .sign(TextEncoder().encode(secretSigningSecret))
        .await()

    private fun customClaims(secretId: SecretId): Record<String, String> = recordOf(
        "https://zegreatrob.com/secret-id" to secretId.value.toString(),
    )
}

external class TextEncoder {
    fun encode(input: String = definedExternally): Uint8Array<*>
}
