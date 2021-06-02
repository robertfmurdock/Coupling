package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.LoggingSyntax
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.external.express.OPEN
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import kotlinx.coroutines.launch
import kotlin.js.Json

data class HandleWebsocketConnectionAction(
    val websocket: WS,
    val request: Request,
    val wss: WebSocketServer,
    val connectionId: String = uuid4().toString()
) : SimpleSuspendAction<HandleWebsocketConnectionActionDispatcher, Unit> {
    override val performFunc = link(HandleWebsocketConnectionActionDispatcher::performItPlease)
}

interface HandleWebsocketConnectionActionDispatcher : ConnectTribeUserCommandDispatcher,
    DisconnectTribeUserCommandDispatcher,
    ReportDocCommandDispatcher,
    LoggingSyntax,
    SuspendActionExecuteSyntax {

    suspend fun performItPlease(action: HandleWebsocketConnectionAction): Result<Unit> = with(action) {
        val tribeId = request.tribeId()
        websocket.tribeId = tribeId.value
        websocket.user = request.user

        websocket.on("message") { message ->
            request.scope.launch {
                logger.info { "Websocket message: $message" }
                val doc = JSON.parse<Json>(message)
                    .fromMessageToPairAssignmentDocument()
                val outgoingMessage = perform(ReportDocCommand(tribeId, doc))
                wss.broadcastConnectionCountForTribe(tribeId, outgoingMessage)
            }
        }
        websocket.on("close") {
            request.scope.launch {
                val message = perform(DisconnectTribeUserCommand(tribeId, connectionId))
                wss.broadcastConnectionCountForTribe(tribeId, message)
            }
        }
        websocket.on("error") { logger.error { it } }

        perform(ConnectTribeUserCommand(tribeId, connectionId))
            ?.let { message -> wss.broadcastConnectionCountForTribe(tribeId, message) }
            ?: websocket.close()
        return Result.success(Unit)
    }

}

fun couplingSocketMessage(connections: List<CouplingConnection>, doc: PairAssignmentDocument?) =
    CouplingSocketMessage(
        "Users viewing this page: ${connections.size}",
        connections.map { it.userPlayer }.toSet(),
        doc
    )

fun Json.fromMessageToPairAssignmentDocument() = this["currentPairAssignments"]
    ?.unsafeCast<Json>()
    ?.toPairAssignmentDocument()

fun WebSocketServer.broadcastConnectionCountForTribe(tribeId: TribeId, message: CouplingSocketMessage) =
    websocketClients()
        .filter { it.tribeId == tribeId.value }
        .broadcast(JSON.stringify(message.toJson()))

fun WebSocketServer.websocketClients(): List<WS> = mutableListOf<WS>()
    .apply { clients.forEach { add(it) } }
    .filter { it.readyState == OPEN }

fun List<WS>.broadcast(content: String) = forEach { it.send(content) }