package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.properties.deg
import react.router.dom.Link
import react.useState
import kotlin.js.Date
import kotlin.random.Random

data class TinyPlayerList(val tribe: Tribe, val players: List<Player>) : DataProps<TinyPlayerList> {
    override val component = tinyPlayerList
}

val tinyPlayerList = tmFC<TinyPlayerList> { (tribe, players) ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)
    players.forEach { player ->
        Link {
            to = tribe.id.with(player).playerConfigPage()
            draggable = false
            val tilt = random.nextInt(7) - 3
            child(PlayerCard(tribe.id, player, linkToConfig = false, size = 40, tilt = tilt.deg), key = player.id)
        }
    }
}