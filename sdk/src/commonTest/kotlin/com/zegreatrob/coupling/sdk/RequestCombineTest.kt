package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.json.JsonCouplingQueryResult
import com.zegreatrob.coupling.json.JsonPartyData
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = asyncSetup(object {
        val performer = StubQueryPerformer()
        val sdk = object : Sdk, PartyGQLPerformer by BatchingPartyGQLPerformer(performer) {
            override suspend fun getToken() = ""
            override val traceId = uuid4()
        }
        val partyId = PartyId("Random")
    }) exercise {
        with(sdk) {
            coroutineScope {
                launch { getPlayers(partyId).elements }
                launch { getPins(partyId) }
            }
        }
    } verify {
        performer.allPostCalls.size.assertIsEqualTo(1)
    }
}

class StubQueryPerformer : QueryPerformer {
    val allPostCalls = mutableListOf<JsonElement>()

    override suspend fun doQuery(queryString: String): JsonElement =
        stubResponseData().apply { allPostCalls.add(JsonPrimitive(queryString)) }

    override suspend fun doQuery(body: JsonElement): JsonElement =
        stubResponseData().apply { allPostCalls.add(body) }

    override fun postAsync(body: JsonElement): Deferred<JsonElement> =
        CompletableDeferred(stubResponseData().apply { allPostCalls.add(body) })

    override suspend fun get(path: String): JsonElement = JsonNull
}

private fun stubResponseData() = Json.encodeToJsonElement(
    JsonCouplingQueryResult(
        partyData = JsonPartyData(),
    ),
)
