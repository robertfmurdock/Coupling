@file:OptIn(ExperimentalEncodingApi::class)

package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CreateSecretCommand
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test

class SdkSecretTest {

    @Test
    fun canGenerateSecret() = asyncSetup(object {
        val party = stubParty()
    }) {
        sdk().perform(SavePartyCommand(party))
    } exercise {
        sdk().perform(CreateSecretCommand(party.id))
    } verify { result ->
        val (secret, token) = (result as SuccessfulResult<Pair<Secret, String>>).value
        secret.assertIsNotEqualTo(null)
        token.assertIsValidToken(party.id)
        sdk().perform(graphQuery { party(party.id) { secretList() } })
            ?.partyData
            ?.secretList
            ?.elements
            .assertIsEqualTo(listOf(secret))
    }

    private fun String.assertIsValidToken(partyId: PartyId) {
        val (header, body, signature) = this.split(".")
        with(header.parseAsObject()) {
            this["alg"]?.jsonPrimitive?.content
                .assertIsEqualTo("HS256")
        }
        body.parseAsObject()["sub"]
            ?.jsonPrimitive
            ?.content
            .assertIsEqualTo(partyId.value)
        signature.assertIsNotEqualTo(null)
    }

    private fun String.parseAsObject() = Base64.decode(this)
        .decodeToString()
        .let(Json.Default::parseToJsonElement)
        .jsonObject
}
