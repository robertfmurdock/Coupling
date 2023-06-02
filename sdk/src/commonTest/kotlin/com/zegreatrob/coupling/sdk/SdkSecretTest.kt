@file:OptIn(ExperimentalEncodingApi::class)

package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CreateSecretCommand
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Ignore
import kotlin.test.Test

class SdkSecretTest {

    @Test
    @Ignore
    fun canGenerateSecret() = asyncSetup(object {
        val party = stubParty()
    }) {
        sdk().perform(SavePartyCommand(party))
    } exercise {
        sdk().perform(CreateSecretCommand(party.id))
    } verify { result ->
        val (secret, token) = (result as SuccessfulResult<Pair<Secret, String>>).value
        secret.assertIsNotEqualTo(null)
        Base64.decode(token)
            .let { Json.parseToJsonElement(it.contentToString()) }
            .jsonObject["sub"]
            ?.jsonPrimitive
            ?.content
            .assertIsEqualTo(party.id.value)
    }
}
