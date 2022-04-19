package com.zegreatrob.coupling.client.external.reactwebsocket

import react.ElementType
import react.Props

@JsModule("react-websocket")
external val websocket: ElementType<WebsocketProps>

external interface WebsocketProps : Props {
    var url: String
    var onMessage: (String) -> Unit
    var onOpen: () -> Unit
    var onClose: () -> Unit
    var ref: (WebsocketComponent) -> Unit
}

external interface WebsocketComponent {
    fun sendMessage(message: Any)
}
