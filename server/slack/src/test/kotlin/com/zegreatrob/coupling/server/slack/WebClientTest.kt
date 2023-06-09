package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.server.slack.external.webclient.LogLevel
import com.zegreatrob.coupling.server.slack.external.webclient.WebClient
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import js.core.jso
import kotlinx.coroutines.await
import kotlin.test.Ignore
import kotlin.test.Test

class WebClientTest {
    val token = ""

    @Test
    @Ignore
    fun canPostMessage() = asyncSetup(object {
        val client = WebClient(token, jso { logLevel = LogLevel.debug })
        val channelId = "C05BWC204S0"
    }) exercise {
        client.chat.postMessage(
            jso {
                channel = channelId
                text = "HEY KOOL AID MAN"
            },
        ).await()
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
    }

    @Test
    @Ignore
    fun canUpdateMessage() = asyncSetup(object {
        val client = WebClient(token, jso { logLevel = LogLevel.debug })
        val channelId = "C05BWC204S0"
        lateinit var ts: String
    }) {
        val response = client.chat.postMessage(jso { channel = channelId; text = "HEY KOOL AID MAN" })
            .await()
        ts = response.ts
    } exercise {
        client.chat.update(jso { ts = this@exercise.ts; channel = channelId; text = "HEY NONG MAN" })
            .await()
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
    }
}
