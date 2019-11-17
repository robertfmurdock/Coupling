package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request

@Suppress("unused")
@JsName("websocketRoute")
val websocketRoute = fun(websocket: WS, request: Request, wss: WebSocketServer) {
    val commandDispatcher = request.commandDispatcher.unsafeCast<CommandDispatcher>()
    commandDispatcher.thing(websocket, request, wss)
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