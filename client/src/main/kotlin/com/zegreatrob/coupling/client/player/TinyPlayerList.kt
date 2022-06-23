package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.components.Paths.playerConfigPage
import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.deg
import react.key
import react.router.dom.Link
import react.useState
import kotlin.js.Date
import kotlin.random.Random

data class TinyPlayerList(val party: Party, val players: List<Player>) : DataPropsBind<TinyPlayerList>(tinyPlayerList)

val tinyPlayerList = tmFC<TinyPlayerList> { (party, players) ->
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
