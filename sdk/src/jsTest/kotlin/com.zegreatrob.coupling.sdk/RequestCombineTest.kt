package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = asyncSetup(object {
        val allPostCalls = mutableListOf<Pair<String, dynamic>>()
        val sdk = object : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(mockAxios(allPostCalls)) {}
        val tribeId = TribeId("Random")
    }) exercise {
        coroutineScope {
            launch { sdk.getPlayers(tribeId) }
            launch { sdk.getPins(tribeId) }
        }
    } verify {
        allPostCalls.size.assertIsEqualTo(1)
    }

    private fun mockAxios(allPostCalls: MutableList<Pair<String, dynamic>>) = json("post" to
            fun(url: String, body: dynamic): Promise<dynamic> {
                allPostCalls.add(url to body)
                return MainScope().promise<dynamic> { stubResponseData() }
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
