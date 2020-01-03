package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = testAsync {
        val allPostCalls = mutableListOf<Pair<String, dynamic>>()
        val mockAxios = json("post" to fun(url: String, body: dynamic): Promise<dynamic> {
            allPostCalls.add(url to body)
            return promise { stubResponseData() }
        }).unsafeCast<Axios>()
        setupAsync(object : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(mockAxios) {
            override val axios: Axios get() = mockAxios
            val tribeId = TribeId("Random")
        }) exerciseAsync {
            coroutineScope {
                val a1 = async { getPlayers(tribeId) }
                val a2 = async { getPins(tribeId) }
                a1.await()
                a2.await()
            }
        } verifyAsync {
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
}