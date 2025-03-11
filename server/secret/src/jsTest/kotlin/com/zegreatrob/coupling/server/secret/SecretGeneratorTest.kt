package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.server.secret.external.jose.get
import com.zegreatrob.coupling.server.secret.external.jose.jwtVerify
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubSecret
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import js.objects.jso
import kotlinx.coroutines.await
import kotlin.random.Random
import kotlin.test.Test

class SecretGeneratorTest {

    private val testSecretSigningSecret = Random.nextBytes(256).decodeToString()

    @Test
    fun canGenerateSecretValue() = asyncSetup(object : JwtSecretGenerator {
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
            jso {
                issuer = arrayOf(this@verify.secretIssuer)
                subject = partyId.value.toString()
                audience = arrayOf(this@verify.secretAudience)
            },
        ).await()
        token.payload["https://zegreatrob.com/secret-id"]
            .assertIsEqualTo(secret.id)
    }
}
