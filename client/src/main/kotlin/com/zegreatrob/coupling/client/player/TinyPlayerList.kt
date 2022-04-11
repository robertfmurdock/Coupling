package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.properties.deg
import react.key
import react.router.dom.Link
import react.useState
import kotlin.js.Date
import kotlin.random.Random

data class TinyPlayerList(val tribe: Party, val players: List<Player>) : DataPropsBind<TinyPlayerList>(tinyPlayerList)

val tinyPlayerList = tmFC<TinyPlayerList> { (tribe, players) ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)
    players.forEach { player ->
        Link {
            key = player.id
            to = tribe.id.with(player).playerConfigPage()
            draggable = false
            val tilt = random.nextInt(7) - 3
            child(PlayerCard(player, size = 40, tilt = tilt.deg))
        }
    }
}