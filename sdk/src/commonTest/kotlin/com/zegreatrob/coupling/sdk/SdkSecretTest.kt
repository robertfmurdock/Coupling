@file:OptIn(ExperimentalEncodingApi::class)

package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.CreateSecretCommand
import com.zegreatrob.coupling.action.DeleteSecretCommand
import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPartyId
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
    fun canGenerateSecretThatCanBeUsedInSdk() = asyncSetup(object {
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

        val tokenSdk = KtorCouplingSdk({ token }, uuid4(), buildClient())
        tokenSdk.perform(graphQuery { party(party.id) { party() } })
            ?.partyData
            ?.party
            ?.data
            .assertIsEqualTo(party)
    }

    @Test
    fun deletingSecretWillPreventTokenFromBeingUsed() = asyncSetup(object {
        val party = stubParty()
        lateinit var secret: Secret
        lateinit var token: String
    }) {
        sdk().perform(SavePartyCommand(party))
        val result = sdk().perform(CreateSecretCommand(party.id)).valueOrNull()!!
        secret = result.first
        token = result.second
    } exercise {
        sdk().perform(DeleteSecretCommand(party.id, secret))
    } verify {
        sdk().perform(graphQuery { party(party.id) { secretList() } })
            ?.partyData
            ?.secretList
            ?.elements
            .assertIsEqualTo(emptyList())
        val tokenSdk = KtorCouplingSdk({ token }, uuid4(), buildClient())
        runCatching { tokenSdk.perform(graphQuery { party(party.id) { party() } }) }
            .exceptionOrNull()
            .assertIsNotEqualTo(null, "Expect this to fail")
    }

    @Test
    fun secretTokenCanOnlySeeRelevantParty() = asyncSetup(object {
        val party1 = stubParty()
        val party2 = stubParty()
        val party3 = stubParty()
    }) {
        listOf(party1, party2, party3)
            .map { SavePartyCommand(it) }
            .forEach { sdk().perform(it) }
    } exercise {
        sdk().perform(CreateSecretCommand(party1.id))
    } verify { result ->
        val (_, token) = (result as SuccessfulResult<Pair<Secret, String>>).value
        val tokenSdk = KtorCouplingSdk({ token }, uuid4(), buildClient())
        tokenSdk.perform(graphQuery { partyList() })
            ?.partyList
            ?.data()
            .assertIsEqualTo(listOf(party1))
    }

    @Test
    fun canNotGenerateSecretForArbitraryParty() = asyncSetup(object {
        val partyId = stubPartyId()
    }) exercise {
        sdk().perform(CreateSecretCommand(partyId))
    } verify { result ->
        result.assertIsEqualTo(NotFoundResult("secret"))
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
