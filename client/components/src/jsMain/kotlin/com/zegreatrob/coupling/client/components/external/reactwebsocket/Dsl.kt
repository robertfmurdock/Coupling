package com.zegreatrob.coupling.client.components.external.reactwebsocket

import kotlinx.js.JsPlainObject
import org.w3c.dom.CloseEvent
import org.w3c.dom.ErrorEvent
import org.w3c.dom.EventSourceInit
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import kotlin.js.Json

@JsModule("react-use-websocket")
external fun useWebSocket(url: String, options: UseWebSocketOptions): UseWebSocket

@JsPlainObject
sealed external interface UseWebSocketOptions {
    val fromSocketIO: Boolean?
    val queryParams: Json?
    val protocols: Array<String>?
    val share: Boolean?
    val reconnectIntervar: Int?
    val reconnectAttempts: Int?
    val retryOnError: Boolean?
    val onOpen: ((event: Event) -> Unit)?
    val onClose: ((event: CloseEvent) -> Unit)?
    val onMessage: ((event: MessageEvent) -> Unit)?
    val onError: ((event: ErrorEvent) -> Unit)?
    val onReconnectStop: ((numAttempts: Int) -> Unit)?
    val shouldReconnect: ((event: CloseEvent) -> Boolean)?
    val filter: ((message: MessageEvent) -> Boolean)?
    val eventSourceOptions: EventSourceInit?
}

external interface UseWebSocket {
    val sendMessage: (message: String, keep: Boolean) -> Unit
}
