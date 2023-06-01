package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.server.secret.external.jose.jwtVerify
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.testmints.async.asyncSetup
import js.core.jso
import kotlinx.coroutines.await
import kotlin.random.Random
import kotlin.test.Test

class SecretGeneratorTest {

    private val testSecretSigningSecret = Random.nextBytes(256).decodeToString()

    @Test
    fun canGenerateSecretValue() = asyncSetup(object : SecretGenerator {
        override val secretIssuer: String = "test-issuer"
        override val secretAudience: String = "https://test.coupling.zegreatrob.com"
        override val secretSigningSecret: String = testSecretSigningSecret
        val partyId = stubPartyId()
        val secretId = uuidString()
    }) exercise {
        createSecret(partyId, secretId)
    } verify { result ->
        jwtVerify(
            result,
            TextEncoder().encode(secretSigningSecret),
            jso {
                issuer = arrayOf(this@verify.secretIssuer)
                subject = partyId.value
                audience = arrayOf(this@verify.secretAudience)
            },
        ).await()
    }
}
