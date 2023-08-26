@file:JsModule("@slack/webhook")

package com.zegreatrob.coupling.server.slack.external.webhook

import kotlin.js.Promise

external class IncomingWebhook(url: String, arguments: WebhookArguments = definedExternally) {
    fun send(message: WebhookMessage): Promise<Unit>
}

sealed external interface WebhookMessage {
    var text: String
}

sealed external interface WebhookArguments {
    @JsName("icon_url")
    var iconUrl: String
}
