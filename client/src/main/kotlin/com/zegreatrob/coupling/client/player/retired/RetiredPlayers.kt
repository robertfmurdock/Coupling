package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.tribe.TribeBrowser
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

data class RetiredPlayers(val tribe: Tribe, val retiredPlayers: List<Player>) :
    DataPropsBind<RetiredPlayers>(com.zegreatrob.coupling.client.player.retired.retiredPlayers)

private val styles = useStyles("player/RetiredPlayers")

val retiredPlayers = tmFC<RetiredPlayers> { (tribe, players) ->
    div {
        className = styles.className
        child(TribeBrowser(tribe))
        div {
            className = styles["header"]
            +"Retired Players"
        }
        div {
            players.forEach { player ->
                child(
                    PlayerCard(tribe.id, player, linkToConfig = true, deselected = true),
                    key = player.id
                )
            }
        }
    }
}
