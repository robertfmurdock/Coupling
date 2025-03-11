package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.dom.Link
import react.useState
import web.cssom.deg
import kotlin.js.Date
import kotlin.random.Random

external interface TinyPlayerListProps : Props {
    var party: PartyDetails
    var players: List<Player>
}

@ReactFunc
val TinyPlayerList by nfc<TinyPlayerListProps> { (party, players) ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)
    players.forEach { player ->
        Link {
            key = player.id.value.toString()
            to = party.id.with(player).playerConfigPage()
            draggable = false
            val tilt = random.nextInt(7) - 3
            PlayerCard(player, size = 40, tilt = tilt.deg)
        }
    }
}
