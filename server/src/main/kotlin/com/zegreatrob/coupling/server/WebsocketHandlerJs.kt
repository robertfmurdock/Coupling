package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.action.LoggingSyntax
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.OPEN
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.route.WS
import com.zegreatrob.coupling.server.route.WebSocketServer
import kotlinx.coroutines.launch
import kotlin.js.json

interface WebsocketHandlerJs : ScopeSyntax, UserIsAuthorizedWithDataActionDispatcher, RequestTribeIdSyntax,
    LoggingSyntax {

    fun thing(
        websocket: WS,
        request: Request,
        wss: WebSocketServer
    ) = scope.launch {
        val tribeId = request.tribeId()
        val result = UserIsAuthorizedWithDataAction(tribeId).perform()
        if (result != null) {
            websocket.tribeId = tribeId.value
            websocket.user = request.user
            websocket.on("close") { broadcastConnectionCountForTribe(tribeId, result.second, wss) }
            websocket.on("error") { logger.error { it } }

            broadcastConnectionCountForTribe(tribeId, result.second, wss)
        } else {
            websocket.close()
        }
    }

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
            )
        )
    }

    private fun toUserPlayerList(matchingConnection: List<WS>, players: List<Player>): List<Player> {
        val uniqueEmails = matchingConnection.map { it.user.email.unsafeCast<String>() }.toSet()
        return uniqueEmails.map { email ->
            val existingPlayer = players.find { it.email == email }

            if (existingPlayer != null) {
                existingPlayer
            } else {
                val atIndex = email.indexOf("@")
                Player("-1", name = email.substring(0, atIndex), email = email)
            }
        }
    }

    fun List<WS>.broadcast(content: String) = forEach { it.send(content) }

    fun connectionIsOpenAndForSameTribe(client: WS, tribeId: TribeId) =
        client.readyState == OPEN && client.tribeId == tribeId.value

}
