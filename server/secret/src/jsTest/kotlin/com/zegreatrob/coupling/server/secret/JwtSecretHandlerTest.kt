package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.server.secret.external.jose.SignJWT
import com.zegreatrob.coupling.server.secret.external.jose.get
import com.zegreatrob.coupling.server.secret.external.jose.jwtVerify
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import js.objects.recordOf
import js.objects.unsafeJso
import kotlinx.coroutines.await
import kotools.types.text.toNotBlankString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

class JwtSecretHandlerTest {

    private val testSecretSigningSecret = Random.nextBytes(256).decodeToString()

    @Test
    fun canGeneratePartySecretValue() = asyncSetup(object : JwtSecretHandler {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val partyId = stubPartyId()
        val secret = stubSecret()
    }) exercise {
        createSecret(partyId.with(secret))
    } verify { result ->
        val token = jwtVerify(
            result,
            TextEncoder().encode(secretSigningSecret),
            unsafeJso {
                issuer = arrayOf(this@verify.secretIssuer)
                subject = partyId.value.toString()
                audience = arrayOf(this@verify.secretAudience)
            },
        ).await()
        token.payload["https://zegreatrob.com/secret-id"]
            .assertIsEqualTo(secret.id.value.toString())
        token.payload["exp"]
            .assertIsEqualTo(null)
    }

    @Test
    fun canGenerateUserIdSecretValue() = asyncSetup(object : JwtSecretHandler {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val userEmail = Uuid.Companion.random().toString().toNotBlankString().getOrThrow()
        val secretId = SecretId.new()
        val expectedExpiration = Clock.System.now().plus(2.hours)
    }) exercise {
        createSecret(Pair(userEmail, secretId))
    } verify { result ->
        val token = jwtVerify(
            result,
            TextEncoder().encode(secretSigningSecret),
            unsafeJso {
                issuer = arrayOf(this@verify.secretIssuer)
                subject = userEmail.toString()
                audience = arrayOf(this@verify.secretAudience)
            },
        ).await()
        token.payload["https://zegreatrob.com/secret-id"]
            .assertIsEqualTo(secretId.value.toString())
        token.payload["exp"]
            .let { expectedExpiration - Instant.fromEpochMilliseconds(convertToMilliseconds(it)) }
            .let {
                (it < 2.seconds)
                    .assertIsEqualTo(
                        true,
                        "Expected expiration of $expectedExpiration to but was off by $it",
                    )
            }
    }

    @Test
    fun validateWillWorkWithNewSecret() = asyncSetup(object : JwtSecretHandler {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val userEmail = Uuid.Companion.random().toString().toNotBlankString().getOrThrow()
        val secretId = SecretId.new()
    }) exercise {
        val token = createSecret(Pair(userEmail, secretId))
        validateSubject(token)
    } verify { result ->
        result?.first.assertIsEqualTo(secretId)
        result?.second.assertIsEqualTo(userEmail.toString())
    }

    @Test
    fun whenSecretIsExpiredValidateWillFail() = asyncSetup(object : JwtSecretHandler {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val userEmail = Uuid.Companion.random().toString().toNotBlankString().getOrThrow()
        val secretId = SecretId.new()
    }) exercise {
        val token = SignJWT(recordOf("https://zegreatrob.com/secret-id" to secretId.value.toString()))
            .setAudience(secretAudience)
            .setSubject(userEmail.toString())
            .setIssuedAt()
            .setExpirationTime("-5 minutes")
            .setIssuer(secretIssuer)
            .setProtectedHeader(unsafeJso { this.alg = "HS256" })
            .sign(TextEncoder().encode(secretSigningSecret))
            .await()
        validateSubject(token)
    } verify { subject ->
        subject.assertIsEqualTo(null)
    }

    @Test
    fun whenAudienceIsWrongValidateWillFail() = asyncSetup(object : JwtSecretHandler {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val userId = UserId.new()
        val secretId = SecretId.new()
    }) exercise {
        val token = SignJWT(recordOf("https://zegreatrob.com/secret-id" to secretId.value.toString()))
            .setAudience("incorrectAudience")
            .setSubject(userId.value.toString())
            .setIssuedAt()
            .setIssuer(secretIssuer)
            .setProtectedHeader(unsafeJso { this.alg = "HS256" })
            .sign(TextEncoder().encode(secretSigningSecret))
            .await()
        validateSubject(token)
    } verify { subject ->
        subject.assertIsEqualTo(null)
    }

    @Test
    fun whenIssueIsWrongValidateWillFail() = asyncSetup(object : JwtSecretHandler {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val userId = UserId.new()
        val secretId = SecretId.new()
    }) exercise {
        val token = SignJWT(recordOf("https://zegreatrob.com/secret-id" to secretId.value.toString()))
            .setAudience(secretAudience)
            .setSubject(userId.value.toString())
            .setIssuedAt()
            .setIssuer("wrong-issuer")
            .setProtectedHeader(unsafeJso { this.alg = "HS256" })
            .sign(TextEncoder().encode(secretSigningSecret))
            .await()
        validateSubject(token)
    } verify { subject ->
        subject.assertIsEqualTo(null)
    }

    private fun convertToMilliseconds(exp: Any?): Long = exp?.unsafeCast<Int>()?.toLong()?.let { it * 1000 } ?: 0
}
