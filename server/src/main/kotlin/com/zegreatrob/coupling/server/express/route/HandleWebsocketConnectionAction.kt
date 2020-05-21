package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.LoggingSyntax
import com.zegreatrob.coupling.actionFunc.DispatchSyntax
import com.zegreatrob.coupling.actionFunc.valueOrNull
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.coupling.server.external.express.OPEN
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import kotlinx.coroutines.launch
import kotlin.js.json

data class HandleWebsocketConnectionAction(val websocket: WS, val request: Request, val wss: WebSocketServer)

interface HandleWebsocketConnectionActionDispatcher : UserIsAuthorizedWithDataActionDispatcher, LoggingSyntax,
    DispatchSyntax {

    fun HandleWebsocketConnectionAction.perform() = request.scope.launch {
        val tribeId = request.tribeId()
        val result = tribeId.getAuthorizationData()
        if (result != null) {
            registerConnection(tribeId, result)
        } else {
            websocket.close()
        }
    }

    private fun HandleWebsocketConnectionAction.registerConnection(
        tribeId: TribeId,
        result: Pair<Any, List<Player>>
    ) {
        websocket.tribeId = tribeId.value
        websocket.user = request.user
        websocket.on("close") { broadcastConnectionCountForTribe(tribeId, result.second, wss) }
        websocket.on("error") { logger.error { it } }

        broadcastConnectionCountForTribe(tribeId, result.second, wss)
    }

    private suspend fun TribeId.getAuthorizationData(): Pair<Any, List<Player>>? =
        execute(UserIsAuthorizedWithDataAction(this)).valueOrNull()

    private fun broadcastConnectionCountForTribe(
        tribeId: TribeId,
        players: List<Player>,
        wss: WebSocketServer
    ) {
        val matchingConnection = mutableListOf<WS>()
            .apply { wss.clients.forEach { add(it) } }
            .filter { connectionIsOpenAndForSameTribe(it, tribeId) }

        matchingConnection.broadcast(
            JSON.stringify(
                json(
                    "type" to "LivePlayers",
                    "text" to "Users viewing this page: ${matchingConnection.size}",
                    "players" to toUserPlayerList(matchingConnection, players).map(Player::toJson)
                )
            ).also { logger.debug { "Broadcasting '$it'" } }
        )
    }

    private fun toUserPlayerList(matchingConnection: List<WS>, players: List<Player>) = matchingConnection
        .map { it.user.email.unsafeCast<String>() }
        .toSet()
        .map { email ->
            val existingPlayer = players.find { it.email == email }

            if (existingPlayer != null) {
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
