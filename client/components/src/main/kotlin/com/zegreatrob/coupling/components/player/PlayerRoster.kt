package com.zegreatrob.coupling.components.player

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.Paths.playerConfigPage
import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.components.large
import com.zegreatrob.coupling.components.orange
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.FontWeight
import csstype.PropertiesBuilder
import csstype.deg
import csstype.em
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import react.useState
import kotlin.js.Date
import kotlin.random.Random

data class PlayerRoster(
    val label: String? = null,
    val players: List<Player>,
    val partyId: PartyId,
    val className: ClassName? = null,
    val cssOverrides: PropertiesBuilder.() -> Unit = {},
) : DataPropsBind<PlayerRoster>(playerRoster)

val playerRoster = tmFC { (label, players, partyId, className, overrides): PlayerRoster ->
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
        addPlayerButton(partyId)
    }
}

private fun ChildrenBuilder.addPlayerButton(partyId: PartyId) = Link {
    to = "/${partyId.value}/player/new/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, orange)) {
        +"Add a new player!"
    }
}
