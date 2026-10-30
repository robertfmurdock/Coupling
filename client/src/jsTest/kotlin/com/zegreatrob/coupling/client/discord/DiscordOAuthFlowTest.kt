package com.zegreatrob.coupling.client.discord

import com.zegreatrob.coupling.action.BeginDiscordOAuthCommand
import com.zegreatrob.coupling.action.BeginDiscordOAuthCommandWrapper
import com.zegreatrob.coupling.action.DiscordOAuthState
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.wrap
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class DiscordOAuthFlowTest {
    @Test
    fun initiationStoresVerifierBeforeReturningStateForNavigation() = asyncSetup(object {
        val received = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(received)
        val storage = mutableMapOf<String, String>()
        val partyId = stubPartyDetails().id
        val flow = DiscordOAuthFlow(cannon, storage::putIgnoringResult, storage::get, storage::removeIgnoringResult)
    }) {
        cannon.givenAny(BeginDiscordOAuthCommandWrapper::class, DiscordOAuthState("state", "expiry"))
    } exercise {
        flow.begin(partyId)
    } verify { state ->
        state.assertIsEqualTo("state")
        val verifier = Json.parseToJsonElement(requireNotNull(storage["discord-oauth:state"])).jsonObject.getValue("verifier").jsonPrimitive.content
        verifier.length.assertIsEqualTo(64)
        received.size.assertIsEqualTo(1)
        (received.single() as BeginDiscordOAuthCommand).partyId.assertIsEqualTo(partyId)
        // SHA-256 is checked against a known vector in a separate test.
        (received.single() as BeginDiscordOAuthCommand).challenge.length.assertIsEqualTo(64)
    }

    @Test
    fun callbackFromAnotherTabDoesNotSubmitGrant() = asyncSetup(object {
        val received = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(received)
        val storage = mutableMapOf<String, String>()
        val flow = DiscordOAuthFlow(cannon, storage::putIgnoringResult, storage::get, storage::removeIgnoringResult)
    }) exercise {
        flow.complete("code", "guild", "unknown-state")
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Rejected)
        received.assertIsEqualTo(emptyList())
    }

    @Test
    fun callbackSubmitsStoredPartyAndVerifierThenClearsOnlyItsFlow() = asyncSetup(object {
        val received = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(received)
        val storage = mutableMapOf<String, String>()
        val partyId = stubPartyDetails().id
        val flow = DiscordOAuthFlow(cannon, storage::putIgnoringResult, storage::get, storage::removeIgnoringResult)
        val command = GrantDiscordAccessCommand("code", "guild", partyId, "state", "verifier")
    }) {
        storage["discord-oauth:state"] = """{"partyId":"${partyId.value}","verifier":"verifier"}"""
        storage["discord-oauth:other"] = "other-flow"
        cannon.given(command.wrap(), VoidResult.Accepted)
    } exercise {
        flow.complete("code", "guild", "state")
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        received.assertIsEqualTo(listOf(command))
        storage.assertIsEqualTo(mapOf("discord-oauth:other" to "other-flow"))
    }

    @Test
    fun rejectedCallbackClearsItsEntryAndPreservesOtherPendingFlow() = asyncSetup(object {
        val received = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(received)
        val storage = mutableMapOf<String, String>()
        val partyId = stubPartyDetails().id
        val flow = DiscordOAuthFlow(cannon, storage::putIgnoringResult, storage::get, storage::removeIgnoringResult)
        val command = GrantDiscordAccessCommand("code", "guild", partyId, "state", "verifier")
    }) {
        storage["discord-oauth:state"] = """{"partyId":"${partyId.value}","verifier":"verifier"}"""
        storage["discord-oauth:other"] = "other-flow"
        cannon.given(command.wrap(), VoidResult.Rejected)
    } exercise {
        flow.complete("code", "guild", "state")
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Rejected)
        received.assertIsEqualTo(listOf(command))
        storage.assertIsEqualTo(mapOf("discord-oauth:other" to "other-flow"))
    }

    @Test
    fun browserChallengeMatchesServerSha256Encoding() = asyncSetup() exercise {
        discordVerifierChallenge("a".repeat(64))
    } verify { result ->
        result.assertIsEqualTo("ffe054fe7ae0cb6dc65c3af9b61d5209f439851db43d0ba5997337df154668eb")
    }
}

private fun MutableMap<String, String>.putIgnoringResult(key: String, value: String) {
    put(key, value)
}
private fun MutableMap<String, String>.removeIgnoringResult(key: String) {
    remove(key)
}
