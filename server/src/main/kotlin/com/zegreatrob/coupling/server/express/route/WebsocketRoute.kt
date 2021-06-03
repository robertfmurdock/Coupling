package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.connection.ConnectTribeUserCommand
import com.zegreatrob.coupling.server.action.connection.DisconnectTribeUserCommand
import com.zegreatrob.coupling.server.action.connection.ReportDocCommand
import com.zegreatrob.coupling.server.external.express.OPEN
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.launch
import kotlin.js.Json

val websocketRoute = fun(websocket: WS, request: Request, wss: WebSocketServer) {
    val connectionId = "${uuid4()}"
    val tribeId = request.query["tribeId"].toString().let(::TribeId)
    websocket.connectionId = connectionId

    val broadcastFunc = broadcastFunc(wss)

    websocket.on("message", messageHandler(request, connectionId, broadcastFunc))
    websocket.on("error") { request.commandDispatcher.logger.error { it } }
    websocket.on("close", closeHandler(request, connectionId, broadcastFunc))

    val broadcastOrClose = broadcastOrClose(websocket, wss)
    val connectHandler = request.dispatch(
        { request.commandDispatcher },
        { ConnectTribeUserCommand(tribeId, connectionId) },
        broadcastOrClose
    )

    connectHandler(null)
}

private fun broadcastOrClose(websocket: WS, wss: WebSocketServer):
            (Pair<List<CouplingConnection>, CouplingSocketMessage>?) -> Unit = { result ->
    when (result) {
        null -> websocket.close()
        else -> wss.broadcastConnectionCountForTribe(result.first, result.second)
    }
}

private fun broadcastFunc(
    wss: WebSocketServer
): (Pair<List<CouplingConnection>, CouplingSocketMessage>?) -> Unit = { result ->
    result?.let { (connections, message) -> wss.broadcastConnectionCountForTribe(connections, message) }
}

private fun messageHandler(
    request: Request, connectionId: String, broadcast: (Pair<List<CouplingConnection>, CouplingSocketMessage>?) -> Unit
) = request.dispatch(
    { request.commandDispatcher },
    { incomingMessage -> ReportDocCommand(connectionId, incomingMessage.parseDocumentMessage()) },
    broadcast
)

private fun closeHandler(
    request: Request, connectionId: String, broadcast: (Pair<List<CouplingConnection>, CouplingSocketMessage>?) -> Unit
) = request.dispatch(
    { request.commandDispatcher },
    { DisconnectTribeUserCommand(connectionId) },
    broadcast
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

    var connectionId: String?
    val readyState: Int
}

fun Json.fromMessageToPairAssignmentDocument() = this["currentPairAssignments"]
    ?.unsafeCast<Json>()
    ?.toPairAssignmentDocument()

private fun WebSocketServer.broadcastConnectionCountForTribe(
    connections: List<CouplingConnection>,
    message: CouplingSocketMessage
) = clientsFor(connections)
    .broadcast(JSON.stringify(message.toJson()))

private fun WebSocketServer.clientsFor(connections: List<CouplingConnection>) = websocketClients()
    .filter { connections.map(CouplingConnection::connectionId).contains(it.connectionId) }

private fun WebSocketServer.websocketClients(): List<WS> = mutableListOf<WS>()
    .apply { clients.forEach { add(it) } }
    .filter { it.readyState == OPEN }

private fun List<WS>.broadcast(content: String) = forEach { it.send(content) }
