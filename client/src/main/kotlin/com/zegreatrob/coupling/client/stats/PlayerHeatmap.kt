package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.stats.heatmap.Heatmap
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.key

data class PlayerHeatmap(val tribe: Tribe, val players: List<Player>, val heatmapData: List<List<Double?>>) :
    DataPropsBind<PlayerHeatmap>(playerHeatmap)

private val styles = useStyles("stats/PlayerHeatmap")

val playerHeatmap = tmFC<PlayerHeatmap> { (tribe, players, heatmapData) ->
    div {
        className = styles["rightSection"]
        div {
            className = styles["heatmapPlayersTopRow"]
            div { className = styles["spacer"] }
            players.map { player ->
                keyedPlayerCard(player, tribe)
            }
        }
        div {
            className = styles["heatmapPlayersSideRow"]
            players.map { player ->
                keyedPlayerCard(player, tribe)
            }
        }
        child(Heatmap(heatmapData, styles["heatmap"]))
    }
}

private fun ChildrenBuilder.keyedPlayerCard(player: Player, tribe: Tribe) = div {
    className = styles["playerCard"]
    key = player.id
    child(PlayerCard(tribe.id, player, size = 50))
}
