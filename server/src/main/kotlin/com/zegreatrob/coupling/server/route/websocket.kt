package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.HandleWebsocketConnectionAction
import com.zegreatrob.coupling.server.external.express.Request

val websocketRoute = fun(websocket: WS, request: Request, wss: WebSocketServer) = with(request.commandDispatcher) {
    HandleWebsocketConnectionAction(websocket, request, wss)
        .perform()
}

external interface WebSocketServer {
    val clients: JsSet
}

external interface JsSet {
    fun forEach(callback: (WS) -> Unit)
}

external interface WS {
    fun on(event: String, callback: (String) -> Unit)
    fun close()
    fun send(content: String)

    var tribeId: String?
    var user: dynamic
    val readyState: Int
}