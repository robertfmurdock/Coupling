package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.RBuilder
import react.dom.div
import react.dom.span

fun RBuilder.serverMessage(tribe: Tribe, message: CouplingSocketMessage) {
    child(ServerMessage(tribe.id, message), key = "${message.text} ${message.players.size}")
}

data class ServerMessage(val tribeId: TribeId, val message: CouplingSocketMessage) : DataProps<ServerMessage> {
    override val component: TMFC<ServerMessage> get() = serverMessage
}

val serverMessage = reactFunction<ServerMessage> { (tribeId, message) ->
    div {
        span { +message.text }
        div {
            message.players.map { child(PlayerCard(tribeId, it, size = 50)) }
        }
    }
}
