package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.server.slack.SlackRequestVerifier
import com.zegreatrob.minassert.assertIsEqualTo
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import kotlin.js.Date
import kotlin.math.roundToInt
import kotlin.test.Test

class SlackCommandTest {

    @Test
    fun canSuccessfullyAcceptSignedSlackRequest() = asyncSetup(object {
        val client = buildClient().config {
            Logging { level = LogLevel.ALL }
        }
        val timestamp = Date.now().roundToInt()
        val formParameters = Parameters.build {
            append("thing", "a")
            append("other-thing", "b")
        }
        val encodedBody = formParameters.formUrlEncode()
        val signature = SlackRequestVerifier("fake")
            .signature(timestamp, encodedBody)
    }) exercise {
        client.submitForm(
            url = "api/integration/slack/command",
            formParameters = formParameters,
        ) {
            header("X-Slack-Request-Timestamp", "$timestamp")
            header("X-Slack-Signature", signature)
        }
    } verify { result ->
        result.status
            .assertIsEqualTo(HttpStatusCode.OK, result.toString())
    }
}
