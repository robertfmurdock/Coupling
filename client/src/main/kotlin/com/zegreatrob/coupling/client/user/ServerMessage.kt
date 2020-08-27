package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.couplingWebsocket
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import react.RProps
import react.dom.div
import react.dom.span


data class ServerMessageProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

val ServerMessage = reactFunction<ServerMessageProps> { (tribeId, useSsl) ->
    couplingWebsocket(tribeId, useSsl) { message, _ ->
        span { +message.text }
        div {
            message.players.map { it.toPlayer() }
                .map { playerCard(PlayerCardProps(tribeId, it, size = 50)) }
        }
    }
}
