@file:JsModule("react-use-websocket")

package com.zegreatrob.coupling.client.components.external.reactwebsocket

import org.w3c.dom.CloseEvent
import org.w3c.dom.ErrorEvent
import org.w3c.dom.EventSourceInit
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import kotlin.js.Json

@JsName("default")
external fun useWebSocket(url: String, options: UseWebSocketOptions): UseWebSocket

sealed external interface UseWebSocketOptions {
    var fromSocketIO: Boolean?
    var queryParams: Json?
    var protocols: Array<String>?
    var share: Boolean?
    var reconnectIntervar: Int?
    var reconnectAttempts: Int?
    var retryOnError: Boolean?
    var onOpen: ((event: Event) -> Unit)?
    var onClose: ((event: CloseEvent) -> Unit)?
    var onMessage: ((event: MessageEvent) -> Unit)?
    var onError: ((event: ErrorEvent) -> Unit)?
    var onReconnectStop: ((numAttempts: Int) -> Unit)?
    var shouldReconnect: ((event: CloseEvent) -> Boolean)?
    var filter: ((message: MessageEvent) -> Boolean)?
    var eventSourceOptions: EventSourceInit?
}

external interface UseWebSocket {
    val sendMessage: (message: String, keep: Boolean) -> Unit
    val sendJsonMessage: (jsonMessage: Json, keep: Boolean) -> Unit
    val lastMessage: MessageEvent?
    val lastJsonMessage: Json?
    val readyState: Short
    val getWebSocket: () -> WebSocket
}
