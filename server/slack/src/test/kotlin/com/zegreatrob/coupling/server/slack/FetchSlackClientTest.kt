package com.zegreatrob.coupling.server.slack

import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Date
import kotlin.test.Ignore
import kotlin.test.Test

@ExperimentalEncodingApi
class FetchSlackClientTest {

    private val token = "xoxb-825373060518-5446975846529-LmDjrjYaJVbA71Y1AQ5HLW2V"

    @Test
    @Ignore
    fun canPostMessage() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
    }) exercise {
        client.postMessage(
            text = "HEY KOOL AID MAN",
            channel = channelId,
            accessToken = token,
        )
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
    }

    @Test
    @Ignore
    fun canPostMessageWithBlock() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
    }) exercise {
        client.postMessage(
            text = "HEY KOOL AID MAN",
            blocks = """[
    {
      "type": "header",
      "text": {
        "type": "plain_text",
        "text": "Couples for 6/19/2023 - 09:19:45",
        "emoji": true
      }
    }
  ]""",
            channel = channelId,
            accessToken = token,
        )
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
    }

    @Test
    @Ignore
    fun canGetConversation() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
        val tenSecondsAgo = Date.now() - 10_000
        val tenSecondsFromNow = Date.now() + 10_000
        var lastMessageTs: String? = null
    }) {
        lastMessageTs = client.postMessage(
            text = "HEY KOOL AID MAN",
            channel = channelId,
            accessToken = token,
        ).ts
    } exercise {
        client.getConversationHistory(
            accessToken = token,
            channel = channelId,
            latest = tenSecondsFromNow,
            oldest = tenSecondsAgo,
        )
    } verify { result ->
        result.ok
            .assertIsEqualTo(true)
        result.messages!!.map { it.ts }
            .assertContains(lastMessageTs)
    }

    @Test
    @Ignore
    fun canUpdateMessage() = asyncSetup(object {
        val client = FetchSlackClient("", "", "")
        val channelId = "C05BWC204S0"
        var ts: String? = null
    }) {
        val response = client.postMessage(
            text = "HEY KOOL AID MAN",
            channel = channelId,
            accessToken = token,
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
