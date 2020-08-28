package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import react.RProps
import react.dom.div
import react.dom.span


data class ServerMessageProps(val tribeId: TribeId, val message: CouplingSocketMessage) : RProps

val ServerMessage = reactFunction<ServerMessageProps> { (tribeId, message) ->
    div {
        span { +message.text }
        div {
            message.players.map { playerCard(PlayerCardProps(tribeId, it, size = 50)) }
        }
    }
}
