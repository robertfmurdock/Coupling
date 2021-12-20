package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.client.child
import react.Props
import react.RBuilder
import react.dom.div
import react.dom.span

fun RBuilder.serverMessage(tribe: Tribe, message: CouplingSocketMessage) {
    child(ServerMessage, ServerMessageProps(tribe.id, message), key = "${message.text} ${message.players.size}")
}

data class ServerMessageProps(val tribeId: TribeId, val message: CouplingSocketMessage) : Props

val ServerMessage = reactFunction<ServerMessageProps> { (tribeId, message) ->
    div {
        span { +message.text }
        div {
            message.players.map { playerCard(PlayerCardProps(tribeId, it, size = 50)) }
        }
    }
}
