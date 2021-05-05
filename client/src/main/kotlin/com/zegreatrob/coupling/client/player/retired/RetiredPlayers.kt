package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import react.RProps
import react.dom.div

data class RetiredPlayersProps(
    val tribe: Tribe,
    val retiredPlayers: List<Player>
) : RProps

private val styles = useStyles("player/RetiredPlayers")

val RetiredPlayers = reactFunction<RetiredPlayersProps> { (tribe, players) ->
    div(classes = styles.className) {
        tribeBrowser(tribe)
        div(classes = styles["header"]) { +"Retired Players" }
        div {
            players.forEach { player ->
                playerCard(
                    PlayerCardProps(tribe.id, player, linkToConfig = true, deselected = true),
                    key = player.id
                )
            }
        }
    }
}
