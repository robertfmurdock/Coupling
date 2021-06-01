package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.LoggingSyntax
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.LiveInfo
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.coupling.server.external.express.OPEN
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.Json

data class HandleWebsocketConnectionAction(val websocket: WS, val request: Request, val wss: WebSocketServer)

interface HandleWebsocketConnectionActionDispatcher : UserIsAuthorizedWithDataActionDispatcher, LoggingSyntax,
    SuspendActionExecuteSyntax {

    val liveInfoRepository: LiveInfoRepository

    fun HandleWebsocketConnectionAction.perform() = request.scope.launch {
        val tribeId = request.tribeId()
        val result = tribeId.getAuthorizationData()
        if (result != null) {
            registerConnection(tribeId, result)
        } else {
            websocket.close()
        }
    }

    private suspend fun HandleWebsocketConnectionAction.registerConnection(
        tribeId: TribeId,
        result: Pair<Any, List<Player>>
    ) {
        websocket.tribeId = tribeId.value
        websocket.user = request.user
        val connectionId = uuid4().toString()
        websocket.connectionId = connectionId

        val connection = CouplingConnection(connectionId, userPlayer(result.second, request.user.email))

        websocket.on("message") { message ->
            MainScope().launch {
                logger.info { "Websocket message: $message" }

                val doc = JSON.parse<Json>(message)
                    .fromMessageToPairAssignmentDocument()

                val info = liveInfoRepository.get(tribeId)
                wss.broadcastConnectionCountForTribe(tribeId, couplingSocketMessage(info, doc))
            }
        }
        websocket.on("close") {
            MainScope().launch {
                val info = removeUserFromInfo(tribeId, connection)
                wss.broadcastConnectionCountForTribe(tribeId, couplingSocketMessage(info, null))
            }
        }
        websocket.on("error") { logger.error { it } }
        val info = addUserToInfo(tribeId, connection)
        wss.broadcastConnectionCountForTribe(tribeId, couplingSocketMessage(info, null))
    }

    private fun Json.fromMessageToPairAssignmentDocument() = this["currentPairAssignments"]
        ?.unsafeCast<Json>()
        ?.toPairAssignmentDocument()

    private suspend fun addUserToInfo(tribeId: TribeId, connection: CouplingConnection) =
        liveInfoRepository.get(tribeId)
            .let { it.copy(connections = it.connections + connection) }
            .also { liveInfoRepository.save(tribeId, it) }

    private suspend fun removeUserFromInfo(tribeId: TribeId, connection: CouplingConnection) =
        liveInfoRepository.get(tribeId)
            .let { it.copy(connections = it.connections - connection) }
            .also { liveInfoRepository.save(tribeId, it) }

    private fun couplingSocketMessage(info: LiveInfo, doc: PairAssignmentDocument?) = CouplingSocketMessage(
        "Users viewing this page: ${info.connections.size}",
        info.connections.map { it.userPlayer }.toSet(),
        doc
    )

    private suspend fun TribeId.getAuthorizationData() = execute(UserIsAuthorizedWithDataAction(this)).valueOrNull()

    private fun WebSocketServer.broadcastConnectionCountForTribe(tribeId: TribeId, message: CouplingSocketMessage) =
        websocketClients()
            .filter { it.tribeId == tribeId.value }
            .broadcast(JSON.stringify(message.toJson()))

    fun WebSocketServer.websocketClients(): List<WS> = mutableListOf<WS>()
        .apply { clients.forEach { add(it) } }
        .filter { it.readyState == OPEN }

    private fun userPlayer(
        players: List<Player>,
        email: String
    ): Player {
        val existingPlayer = players.find { it.email == email }

        return if (existingPlayer != null) {
            existingPlayer
        } else {
            val atIndex = email.indexOf("@")
            Player("-1", name = email.substring(0, atIndex), email = email)
        }
    }

    fun List<WS>.broadcast(content: String) = forEach { it.send(content) }

    fun connectionIsOpenAndForSameTribe(client: WS, tribeId: TribeId) =
        client.readyState == OPEN && client.tribeId == tribeId.value

}
