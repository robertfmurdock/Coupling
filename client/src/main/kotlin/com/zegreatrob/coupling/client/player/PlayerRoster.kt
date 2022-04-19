package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.RuleSet
import kotlinx.css.properties.deg
import kotlinx.html.classes
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.key
import react.router.dom.Link
import react.useState
import kotlin.js.Date
import kotlin.random.Random

data class PlayerRoster(
    val label: String? = null,
    val players: List<Player>,
    val partyId: PartyId,
    val className: String? = null,
    val cssOverrides: RuleSet = {}
) : DataPropsBind<PlayerRoster>(playerRoster)

private val styles = useStyles("player/PlayerRoster")

val playerRoster = tmFC { (label, players, partyId, className, overrides): PlayerRoster ->
    val ref by useState { Date.now().toLong() }
    val random = Random(ref)

    cssDiv(
        attrs = {
            if (className != null) classes = classes + className
            classes = classes + styles.className.toString()
        },
        css = { overrides() }
    ) {
        div {
            if (players.isNotEmpty()) {
                div {
                    this.className = styles["header"]
                    +(label ?: "Players")
                }
                players.map { player ->
                    Link {
                        to = partyId.with(player).playerConfigPage()
                        draggable = false
                        key = player.id
                        val tilt = random.nextInt(7) - 3
                        child(PlayerCard(player, tilt = tilt.deg))
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
    child(CouplingButton(large, orange, styles["addPlayerButton"])) {
        +"Add a new player!"
    }
}
