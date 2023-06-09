package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.server.slack.external.webhook.IncomingWebhook
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import js.core.jso
import kotlinx.coroutines.await
import kotlin.test.Ignore
import kotlin.test.Test

class WebhookTest {

    @Test
    @Ignore
    fun canSendMessageViaHook() = asyncSetup(object {
        val hookUrl = ""
        val webhook = IncomingWebhook(
            url = hookUrl,
            arguments = jso { iconUrl = "https://assets.zegreatrob.com/coupling/1.0.1970/html/assets/favicon.ico" },
        )
    }) exercise {
        webhook.send(jso { text = "OH YEAH" })
            .await()
    } verify { result ->
        result.assertIsEqualTo(null)
    }
}
