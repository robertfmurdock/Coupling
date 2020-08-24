package com.zegreatrob.coupling.client.external.reactwebsocket

import react.RClass
import react.RProps

@JsModule("react-websocket")
external val websocket: RClass<WebsocketProps>

external interface WebsocketProps : RProps {
    var url: String
    var onMessage: (String) -> Unit
    var onOpen: () -> Unit
    var onClose: () -> Unit
    var ref: (WebsocketComponent) -> Unit

}

external interface WebsocketComponent {
    fun sendMessage(message: Any)
}
