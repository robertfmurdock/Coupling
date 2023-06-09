@file:JsModule("@slack/webhook")

package com.zegreatrob.coupling.server.slack.external.webhook

import kotlin.js.Promise

external class IncomingWebhook(url: String, arguments: WebhookArguments = definedExternally) {
    fun send(message: WebhookMessage): Promise<Unit>
}

external interface WebhookMessage {
    var text: String
}

external interface WebhookArguments {
    @JsName("icon_url")
    var iconUrl: String
}
