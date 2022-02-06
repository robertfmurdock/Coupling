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
        val allPostCalls = mutableListOf<JsonElement>()
        val sdk = object : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(object: QueryPerformer {

          override  suspend fun doQuery(body: String): JsonElement {
              return stubResponseData().apply { allPostCalls.add(JsonPrimitive( body)) }
          }
          override  suspend fun doQuery(body: JsonElement): JsonElement {
              return stubResponseData().apply { allPostCalls.add(body) }
          }
          override  fun postAsync(body: JsonElement): Deferred<JsonElement> {
              return CompletableDeferred(stubResponseData().apply { allPostCalls.add(body) })
          }

            override suspend fun get(path: String): JsonElement = JsonNull
        }) {}
        val tribeId = TribeId("Random")
    }) exercise {
        coroutineScope {
            launch { sdk.playerRepository.getPlayers(tribeId) }
            launch { sdk.pinRepository.getPins(tribeId) }
        }
    } verify {
        allPostCalls.size.assertIsEqualTo(1)
    }

}

private fun stubResponseData() = buildJsonObject {
    putJsonObject("data") {
        putJsonObject("data") {
            putJsonObject("tribe") {
               putJsonArray("playerList"){}
               putJsonArray("pinList"){}
            }
        }
    }
}