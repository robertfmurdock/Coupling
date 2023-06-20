package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.client.components.PlayerCard
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import react.router.dom.Link
import react.useState
import web.cssom.deg
import kotlin.js.Date
import kotlin.random.Random

data class TinyPlayerList(val party: PartyDetails, val players: List<Player>) : DataPropsBind<TinyPlayerList>(tinyPlayerList)

val tinyPlayerList by ntmFC<TinyPlayerList> { (party, players) ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)
    players.forEach { player ->
        Link {
            key = player.id
            to = party.id.with(player).playerConfigPage()
            draggable = false
            val tilt = random.nextInt(7) - 3
            add(PlayerCard(player, size = 40, tilt = tilt.deg))
        }
    }
}
