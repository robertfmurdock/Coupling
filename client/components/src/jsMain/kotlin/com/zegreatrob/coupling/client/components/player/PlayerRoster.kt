package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.Paths.playerConfigPage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import react.useState
import web.cssom.ClassName
import web.cssom.FontWeight
import web.cssom.deg
import web.cssom.em
import kotlin.js.Date
import kotlin.random.Random

external interface PlayerRosterProps : Props {
    var label: String?
    var players: List<Player>

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var className: ClassName?
    var cssOverrides: ((PropertiesBuilder) -> Unit)?
}

@ReactFunc
val PlayerRoster by nfc<PlayerRosterProps> { (label, players, partyId, className, overrides) ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)

    div {
        css(className) { overrides?.invoke(this) }
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
                        key = player.id.value.toString()
                        val tilt = random.nextInt(7) - 3
                        PlayerCard(player, tilt = tilt.deg)
                    }
                }
            }
        }
    }
}
