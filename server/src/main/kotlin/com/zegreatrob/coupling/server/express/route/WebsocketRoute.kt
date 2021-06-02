package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.launch
import kotlin.js.Json

val websocketRoute = fun(websocket: WS, request: Request, wss: WebSocketServer) {
    val connectionId = "${uuid4()}"
    val tribeId = request.query["tribeId"].toString().let(::TribeId)
    websocket.tribeId = tribeId.value
    websocket.user = request.user
    websocket.connectionId = connectionId

    websocket.on("message", messageHandler(request, tribeId, wss))
    websocket.on("error") { request.commandDispatcher.logger.error { it } }

    websocket.on("close", closeHandler(request, tribeId, connectionId, wss))

    val connectHandler = request.dispatch(
        { request.commandDispatcher },
        { ConnectTribeUserCommand(tribeId, connectionId) },
        { message ->
            when (message) {
                null -> websocket.close()
                else -> wss.broadcastConnectionCountForTribe(tribeId, message)
            }
        }
    )

    connectHandler(null)
}

private fun messageHandler(request: Request, tribeId: TribeId, wss: WebSocketServer) = request.dispatch(
    { request.commandDispatcher },
    { incomingMessage -> ReportDocCommand(tribeId, incomingMessage.parseDocumentMessage()) },
    { message -> wss.broadcastConnectionCountForTribe(tribeId, message) }
)

private fun closeHandler(request: Request, tribeId: TribeId, connectionId: String, wss: WebSocketServer) =
    request.dispatch(
        { request.commandDispatcher },
        { DisconnectTribeUserCommand(tribeId, connectionId) },
        { message -> wss.broadcastConnectionCountForTribe(tribeId, message) }
    )

private fun String?.parseDocumentMessage() = JSON.parse<Json>(this ?: "")
    .fromMessageToPairAssignmentDocument()

fun <D : SuspendActionExecuteSyntax, Q : SuspendAction<D, R>, R> Request.dispatch(
    dispatcherFunc: () -> D,
    queryFunc: (String?) -> Q,
    socketResponseFunc: (R) -> Unit
): (String?) -> Unit = { args: String? ->
    scope.launch {
        val command = queryFunc(args)
        dispatcherFunc()
            .execute(command)
            .let(socketResponseFunc)
    }
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
