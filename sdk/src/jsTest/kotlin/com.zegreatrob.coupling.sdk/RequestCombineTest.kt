package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync2
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = setupAsync2(object {
        val allPostCalls = mutableListOf<Pair<String, dynamic>>()
        val sdk = object : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(mockAxios(allPostCalls)) {}
        val tribeId = TribeId("Random")
    }) exercise {
        coroutineScope {
            val a1 = async { sdk.getPlayers(tribeId) }
            val a2 = async { sdk.getPins(tribeId) }
            a1.await()
            a2.await()
        }
    } verify {
        allPostCalls.size.assertIsEqualTo(1)
    }

    private fun mockAxios(allPostCalls: MutableList<Pair<String, dynamic>>) = json("post" to
            fun(url: String, body: dynamic): Promise<dynamic> {
                allPostCalls.add(url to body)
                return GlobalScope.promise<dynamic> { stubResponseData() }
            }
    ).unsafeCast<Axios>()
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
