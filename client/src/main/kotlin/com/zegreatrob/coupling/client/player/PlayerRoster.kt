package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.RuleSet
import kotlinx.html.classes
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class PlayerRoster(
    val label: String? = null,
    val players: List<Player>,
    val tribeId: TribeId,
    val className: String? = null,
    val cssOverrides: RuleSet = {}
) : DataProps<PlayerRoster> {
    override val component: TMFC<PlayerRoster> get() = playerRoster
}

private val styles = useStyles("player/PlayerRoster")

val playerRoster = tmFC { (label, players, tribeId, className, overrides): PlayerRoster ->
    cssDiv(
        attrs = {
            if (className != null) classes = classes + className
            classes = classes + styles.className
        },
        css = { overrides() }
    ) {
        div {
            div {
                this.className = styles["header"]
                +(label ?: "Players")
            }
            renderPlayers(players, tribeId)
                .forEach(::child)
        }
        addPlayerButton(tribeId)
    }
}

private fun ChildrenBuilder.addPlayerButton(tribeId: TribeId) = Link {
    to = "/${tribeId.value}/player/new/"
    child(CouplingButton(large, orange, styles["addPlayerButton"])) {
        +"Add a new player!"
    }
}

private fun renderPlayers(players: List<Player>, tribeId: TribeId) = players.map { player ->
    create(
        PlayerCard(tribeId, player, linkToConfig = true),
        key = player.id
    )
}
