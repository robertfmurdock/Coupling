package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.stats.heatmap.Heatmap
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.number
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.key

data class PlayerHeatmap(val players: List<Player>, val heatmapData: List<List<Double?>>) :
    DataPropsBind<PlayerHeatmap>(playerHeatmap)

private val styles = useStyles("stats/PlayerHeatmap")

val playerHeatmap = tmFC<PlayerHeatmap> { (players, heatmapData) ->
    ReactHTML.div {
        css(styles["rightSection"]) {
            whiteSpace = csstype.WhiteSpace.nowrap
            flexShrink = number(0.0)
        }
        div {
            className = styles["heatmapPlayersTopRow"]
            div { className = styles["spacer"] }
            players.map { player ->
                keyedPlayerCard(player)
            }
        }
        div {
            className = styles["heatmapPlayersSideRow"]
            players.map { player ->
                keyedPlayerCard(player)
            }
        }
        add(Heatmap(heatmapData, "${styles["heatmap"]}"))
    }
}

private fun ChildrenBuilder.keyedPlayerCard(player: Player) = div {
    className = styles["playerCard"]
    key = player.id
    add(PlayerCard(player, size = 50))
}
