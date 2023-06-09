@file:JsModule("@slack/web-api")

package com.zegreatrob.coupling.server.slack.external.webclient

import kotlin.js.Promise

external class WebClient(token: String, option: WebClientOptions = definedExternally) {

    val chat: WebClientChat
}

external interface WebClientChat {
    fun postMessage(options: ChatPostOptions): Promise<WebClientChatPostResponse>
    fun update(jso: ChatUpdateOptions): Promise<WebClientChatPostResponse>
}

external interface WebClientChatPostResponse {
    val ok: Boolean
    val channel: String
    val ts: String
}

external interface ChatPostOptions {
    var channel: String
    var text: String
}

external interface ChatUpdateOptions {
    var ts: String
    var channel: String
    var text: String
}

external interface WebClientOptions {
    var logLevel: BoltLogLevel
}

external object LogLevel {
    @JsName("DEBUG")
    val debug: BoltLogLevel
}

external interface BoltLogLevel
