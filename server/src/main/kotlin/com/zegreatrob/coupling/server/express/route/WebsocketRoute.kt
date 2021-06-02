package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.launch
import kotlin.js.Json

val websocketRoute = fun(websocket: WS, request: Request, wss: WebSocketServer) {
    val connectionId = "${uuid4()}"
    val tribeId = request.tribeId()
    websocket.tribeId = tribeId.value
    websocket.user = request.user
    websocket.connectionId = connectionId

    val messageHandler = request.dispatch(
        { request.commandDispatcher },
        { incomingMessage -> ReportDocCommand(tribeId, incomingMessage.parseDocumentMessage()) },
        { message -> wss.broadcastConnectionCountForTribe(tribeId, message) })

    websocket.on("message", messageHandler)
    websocket.on("error") { request.commandDispatcher.logger.error { it } }

    val closeHandler = request.dispatch(
        { request.commandDispatcher },
        { DisconnectTribeUserCommand(tribeId, connectionId) },
        { message -> wss.broadcastConnectionCountForTribe(tribeId, message) })

    websocket.on("close", closeHandler)

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
