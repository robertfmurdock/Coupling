package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.launch

val websocketRoute = fun(websocket: WS, request: Request, wss: WebSocketServer) = request.scope.launch {
    request.commandDispatcher.execute(HandleWebsocketConnectionAction(websocket, request, wss))
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
    var connectionId: String?
    val readyState: Int
}
