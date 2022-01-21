package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class ServerMessage(val tribeId: TribeId, val message: CouplingSocketMessage) :
    DataPropsBind<ServerMessage>(serverMessage)

val serverMessage = tmFC<ServerMessage> { (tribeId, message) ->
    div {
        span { +message.text }
        div {
            message.players.map { child(PlayerCard(tribeId, it, size = 50)) }
        }
    }
}
