@file:OptIn(ExperimentalEncodingApi::class)

package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.secret.fire
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class SdkSecretTest {

    @Test
    fun canGenerateSecretThatCanBeUsedInSdk() = asyncSetup(object {
        val party = stubPartyDetails()
        val secretDescription = uuidString()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        sdk().fire(CreateSecretCommand(party.id, secretDescription))
    } verify { result ->
        val (secret, token) = result!!
        secret.assertIsNotEqualTo(null)
        token.assertIsValidToken(party.id)
        sdk().fire(graphQuery { party(party.id) { secretList() } })
            ?.party
            ?.secretList
            ?.elements
            .assertIsEqualTo(listOf(secret))

        val tokenSdk = couplingSdk({ token }, buildClient())
        tokenSdk.fire(graphQuery { party(party.id) { details() } })
            ?.party
            ?.details
            ?.data
            .assertIsEqualTo(party)
    }

    @Test
    fun whenSecretIsUsedTheLastUsedTimestampIsUpdated() = asyncSetup(object {
        val party = stubPartyDetails()
        val secretDescription = uuidString()
        lateinit var tokenSdk: DispatcherPipeCannon<CouplingSdkDispatcher>
    }) {
        sdk().fire(SavePartyCommand(party))
        val (_, token) = sdk().fire(CreateSecretCommand(party.id, secretDescription))!!
        tokenSdk = couplingSdk({ token }, buildClient())
    } exercise {
        tokenSdk.fire(graphQuery { party(party.id) { details() } })
        sdk().fire(graphQuery { party(party.id) { secretList() } })
            ?.party
            ?.secretList
            ?.elements
    } verify { result ->
        result?.first()?.lastUsedTimestamp
            ?.let { Clock.System.now() - it }
            .let { delta ->
                delta?.let { delta < 0.3.seconds }.assertIsEqualTo(
                    true,
                    "lastUsedTimestamp should have been now-ish, but was $delta earlier, at ${result?.first()?.lastUsedTimestamp}",
                )
            }
    }

    @Test
    fun deletingSecretWillPreventTokenFromBeingUsed() = asyncSetup(object {
        val party = stubPartyDetails()
        val secretDescription = uuidString()
        lateinit var secret: Secret
        lateinit var token: String
    }) {
        sdk().fire(SavePartyCommand(party))
        val result = sdk().fire(CreateSecretCommand(party.id, secretDescription))!!
        secret = result.first
        token = result.second
    } exercise {
        sdk().fire(DeleteSecretCommand(party.id, secret.id))
    } verify {
        sdk().fire(graphQuery { party(party.id) { secretList() } })
            ?.party
            ?.secretList
            ?.elements
            .assertIsEqualTo(emptyList())
        val tokenSdk = couplingSdk({ token }, buildClient())
        runCatching { tokenSdk.fire(graphQuery { party(party.id) { details() } }) }
            .exceptionOrNull()
            .assertIsNotEqualTo(null, "Expect this to fail")
    }

    @Test
    fun secretTokenCanOnlySeeRelevantParty() = asyncSetup(object {
        val party1 = stubPartyDetails()
        val party2 = stubPartyDetails()
        val party3 = stubPartyDetails()
        val secretDescription = uuidString()
    }) {
        listOf(party1, party2, party3)
            .map { SavePartyCommand(it) }
            .forEach { sdk().fire(it) }
    } exercise {
        sdk().fire(CreateSecretCommand(party1.id, secretDescription))
    } verify { result ->
        val (_, token) = result!!
        val tokenSdk = couplingSdk({ token }, buildClient())
        val queryResult: CouplingQueryResult? = tokenSdk.fire(
            graphQuery {
                partyList { details() }
                party(party2.id) { details() }
            },
        )
        queryResult
            ?.partyList
            ?.mapNotNull { it.details }
            ?.data()
            .assertIsEqualTo(listOf(party1))
        queryResult?.party
            .assertIsEqualTo(null)
    }

    @Test
    fun canNotGenerateSecretForArbitraryParty() = asyncSetup(object {
        val partyId = stubPartyId()
        val secretDescription = uuidString()
    }) exercise {
        sdk().fire(CreateSecretCommand(partyId, secretDescription))
    } verify { result ->
        result.assertIsEqualTo(null)
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
            .assertIsEqualTo(partyId.value.toString())
        signature.assertIsNotEqualTo(null)
    }

    private fun String.parseAsObject() = Base64.withPadding(option = Base64.PaddingOption.PRESENT_OPTIONAL)
        .decode(this)
        .decodeToString()
        .let(Json.Default::parseToJsonElement)
        .jsonObject
}
