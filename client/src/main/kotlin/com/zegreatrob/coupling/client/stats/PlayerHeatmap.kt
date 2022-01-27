package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.stats.heatmap.Heatmap
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.WhiteSpace
import kotlinx.css.flexShrink
import kotlinx.css.whiteSpace
import kotlinx.html.classes
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.key

data class PlayerHeatmap(val players: List<Player>, val heatmapData: List<List<Double?>>) :
    DataPropsBind<PlayerHeatmap>(playerHeatmap)

private val styles = useStyles("stats/PlayerHeatmap")

val playerHeatmap = tmFC<PlayerHeatmap> { (players, heatmapData) ->
    cssDiv(css = {
        whiteSpace = WhiteSpace.nowrap
        flexShrink = 0.0
    }, attrs = { classes = setOf(styles["rightSection"]) }) {
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
        child(Heatmap(heatmapData, styles["heatmap"]))
    }
}

private fun ChildrenBuilder.keyedPlayerCard(player: Player) = div {
    className = styles["playerCard"]
    key = player.id
    child(PlayerCard(player, size = 50))
}
