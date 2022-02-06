package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = asyncSetup(object {
        val performer = StubQueryPerformer1()
        val sdk = object : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(performer) {
            override suspend fun getToken() = ""
        }
        val tribeId = TribeId("Random")
    }) exercise {
        with(sdk) {
            coroutineScope {
                launch { tribeId.getPlayerList() }
                launch { tribeId.getPins() }
            }
        }
    } verify {
        performer.allPostCalls.size.assertIsEqualTo(1)
    }

}

class StubQueryPerformer1 : QueryPerformer {
    val allPostCalls = mutableListOf<JsonElement>()

    override suspend fun doQuery(body: String): JsonElement =
        stubResponseData().apply { allPostCalls.add(JsonPrimitive(body)) }

    override suspend fun doQuery(body: JsonElement): JsonElement =
        stubResponseData().apply { allPostCalls.add(body) }

    override fun postAsync(body: JsonElement): Deferred<JsonElement> =
        CompletableDeferred(stubResponseData().apply { allPostCalls.add(body) })

    override suspend fun get(path: String): JsonElement = JsonNull
}

private fun stubResponseData() = buildJsonObject {
    putJsonObject("data") {
        putJsonObject("data") {
            putJsonObject("tribe") {
                putJsonArray("playerList") {}
                putJsonArray("pinList") {}
            }
        }
    }
}