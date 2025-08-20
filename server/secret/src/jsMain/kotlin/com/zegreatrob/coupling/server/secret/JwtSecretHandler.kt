package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.server.action.PartySecretGenerator
import com.zegreatrob.coupling.server.action.SecretValidator
import com.zegreatrob.coupling.server.action.UserSecretGenerator
import com.zegreatrob.coupling.server.secret.external.jose.SignJWT
import com.zegreatrob.coupling.server.secret.external.jose.get
import com.zegreatrob.coupling.server.secret.external.jose.jwtVerify
import js.objects.Record
import js.objects.recordOf
import js.objects.unsafeJso
import js.typedarrays.Uint8Array
import kotlinx.coroutines.await
import kotools.types.text.NotBlankString

interface JwtSecretHandler :
    PartySecretGenerator,
    UserSecretGenerator,
    SecretValidator {

    val secretIssuer: String
    val secretSigningSecret: String
    val secretAudience: String

    override suspend fun createSecret(secret: PartyElement<Secret>) = jwt(
        subject = secret.partyId.value,
        secretId = secret.element.id,
        exp = null,
    )

    override suspend fun createSecret(secret: Pair<NotBlankString, SecretId>): String = jwt(
        secret.first,
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

    override suspend fun validateSubject(secret: String): Pair<SecretId, String>? = try {
        jwtVerify(
            token = secret,
            secret = TextEncoder().encode(secretSigningSecret),
            options = unsafeJso {
                audience = arrayOf(secretAudience)
                issuer = arrayOf(secretIssuer)
            },
        )
            .await()
            .payload
            .let {
                Pair(
                    first = it["https://zegreatrob.com/secret-id"]
                        ?.toString()
                        ?.let(::SecretId)
                        ?: return null,
                    second = it.sub,
                )
            }
    } catch (_: Throwable) {
        null
    }
}

external class TextEncoder {
    fun encode(input: String = definedExternally): Uint8Array<*>
}
