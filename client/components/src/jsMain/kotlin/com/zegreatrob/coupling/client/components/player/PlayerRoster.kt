package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import csstype.PropertiesBuilder
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import react.useState
import web.cssom.ClassName
import web.cssom.FontWeight
import web.cssom.deg
import web.cssom.em
import kotlin.js.Date
import kotlin.random.Random

data class PlayerRoster(
    val label: String? = null,
    val players: List<Player>,
    val partyId: PartyId,
    val className: ClassName? = null,
    val cssOverrides: PropertiesBuilder.() -> Unit = {},
) : DataPropsBind<PlayerRoster>(playerRoster)

val playerRoster by ntmFC { (label, players, partyId, className, overrides): PlayerRoster ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)

    div {
        css(className) {
            overrides()
        }
        div {
            if (players.isNotEmpty()) {
                div {
                    css {
                        fontSize = 1.5.em
                        fontWeight = FontWeight.bold
                        asDynamic()["margin-before"] = "0.2em"
                        asDynamic()["margin-after"] = "0.58em"
                    }
                    +(label ?: "Players")
                }
                players.map { player ->
                    Link {
                        to = partyId.with(player).playerConfigPage()
                        draggable = false
                        key = player.id
                        val tilt = random.nextInt(7) - 3
                        add(PlayerCard(player, tilt = tilt.deg))
                    }
                }
            }
        }
    }
}