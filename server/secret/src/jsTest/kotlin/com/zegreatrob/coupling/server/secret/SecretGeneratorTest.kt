package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.server.secret.external.jose.get
import com.zegreatrob.coupling.server.secret.external.jose.jwtVerify
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import js.objects.unsafeJso
import kotlinx.coroutines.await
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class SecretGeneratorTest {

    private val testSecretSigningSecret = Random.nextBytes(256).decodeToString()

    @Test
    fun canGeneratePartySecretValue() = asyncSetup(object : JwtSecretGenerator {
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
    fun canGenerateUserIdSecretValue() = asyncSetup(object : JwtSecretGenerator {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val userId = UserId.new()
        val secretId = SecretId.new()
        val expectedExpiration = Clock.System.now().plus(2.hours)
    }) exercise {
        createSecret(Pair(userId, secretId))
    } verify { result ->
        val token = jwtVerify(
            result,
            TextEncoder().encode(secretSigningSecret),
            unsafeJso {
                issuer = arrayOf(this@verify.secretIssuer)
                subject = userId.value.toString()
                audience = arrayOf(this@verify.secretAudience)
            },
        ).await()
        token.payload["https://zegreatrob.com/secret-id"]
            .assertIsEqualTo(secretId.value.toString())
        token.payload["exp"]
            .let { expectedExpiration - Instant.fromEpochMilliseconds(convertToMilliseconds(it)) }
            .let {
                (it < 5.milliseconds)
                    .assertIsEqualTo(
                        true,
                        "Expected expiration of $expectedExpiration to be within 5ms of now but was $it",
                    )
            }
    }

    private fun convertToMilliseconds(exp: Any?): Long = exp?.unsafeCast<Int>()?.toLong()?.let { it * 1000 } ?: 0
}
