@file:JsModule("@slack/webhook")

package com.zegreatrob.coupling.server.slack.external.webhook

import kotlinx.js.JsPlainObject
import kotlin.js.Promise

external class IncomingWebhook(url: String, arguments: WebhookArguments = definedExternally) {
    fun send(message: WebhookMessage): Promise<Unit>
}

@JsPlainObject
sealed external interface WebhookMessage {
    val text: String
}

@JsPlainObject
sealed external interface WebhookArguments {
    @JsName("icon_url")
    val iconUrl: String
}
