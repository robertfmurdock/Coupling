package com.zegreatrob.coupling.server.slack

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Ignore
import kotlin.test.Test

class FetchSlackClientTest {
    val token = ""

    @Test
    @Ignore
    fun canPostMessage() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
    }) exercise {
        runCatching {
            client.sendMessage(
                accessToken = token,
                channel = channelId,
                message = "HEY KOOL AID MAN",
            )
        }
    } verify { result ->
        result.isSuccess
            .assertIsEqualTo(true)
    }

    // @Test
    // @Ignore
    // fun canUpdateMessage() = asyncSetup(object {
    //     val client = FetchSlackClient("", "", "")
    //     val channelId = "C05BWC204S0"
    //     lateinit var ts: String
    // }) {
    //     val response = client.chat.postMessage(jso { channel = channelId; text = "HEY KOOL AID MAN" })
    //         .await()
    //     ts = response.ts
    // } exercise {
    //     client.chat.update(jso { ts = this@exercise.ts; channel = channelId; text = "HEY NONG MAN" })
    //         .await()
    // } verify { result ->
    //     result.ok
    //         .assertIsEqualTo(true)
    // }
}
