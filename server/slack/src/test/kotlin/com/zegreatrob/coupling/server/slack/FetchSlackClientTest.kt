package com.zegreatrob.coupling.server.slack

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Ignore
import kotlin.test.Test

@ExperimentalEncodingApi
class FetchSlackClientTest {

    private val token = ""

    @Test
    @Ignore
    fun canPostMessage() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
    }) exercise {
        client.postMessage(
            accessToken = token,
            channel = channelId,
            message = "HEY KOOL AID MAN",
        )
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
    }

    @Test
    @Ignore
    fun canUpdateMessage() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
        var ts: String? = null
    }) {
        val response = client.postMessage(
            accessToken = token,
            channel = channelId,
            message = "HEY KOOL AID MAN",
        )
        ts = response.ts
    } exercise {
        client.updateMessage(
            accessToken = token,
            channel = channelId,
            ts = this@exercise.ts,
            text = "HEY NONG MAN",
        )
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
    }
}
