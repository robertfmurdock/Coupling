package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = asyncSetup(object {
        val allPostCalls = mutableListOf<dynamic>()
        val sdk = object : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(object: QueryPerformer {

          override  suspend fun doQuery(body: String): dynamic {
              return stubResponseData().apply { allPostCalls.add(body) }
          }
          override  suspend fun doQuery(body: Json): dynamic {
              return stubResponseData().apply { allPostCalls.add(body) }
          }
          override  fun postAsync(body: dynamic): Deferred<Json> {
              return CompletableDeferred(stubResponseData().apply { allPostCalls.add(body) })
          }

            override suspend fun get(path: String): dynamic {
                return null
            }
        }) {}
        val tribeId = TribeId("Random")
    }) exercise {
        coroutineScope {
            launch { sdk.getPlayers(tribeId) }
            launch { sdk.getPins(tribeId) }
        }
    } verify {
        allPostCalls.size.assertIsEqualTo(1)
    }

}

private fun stubResponseData() = json(
    "data" to json(
        "data" to json(
            "tribe" to json(
                "playerList" to emptyArray<Json>(),
                "pinList" to emptyArray<Json>()
            )
        )
    )
)
