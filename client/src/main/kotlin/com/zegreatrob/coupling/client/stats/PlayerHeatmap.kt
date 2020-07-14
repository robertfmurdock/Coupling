package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.render
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.stats.heatmap.HeatmapProps
import com.zegreatrob.coupling.client.stats.heatmap.heatmap
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.DIV
import react.RBuilder
import react.RProps
import react.dom.RDOMBuilder
import react.dom.div

val RBuilder.playerHeatmap get() = PlayerHeatmap.render(this)

data class PlayerHeatmapProps(
    val tribe: Tribe,
    val players: List<Player>,
    val heatmapData: List<List<Double?>>
) : RProps

private val styles = useStyles("stats/PlayerHeatmap")

val PlayerHeatmap = reactFunction<PlayerHeatmapProps> { (tribe, players, heatmapData) ->
    div(classes = styles["rightSection"]) {
        div(classes = styles["heatmapPlayersTopRow"]) {
            div(classes = styles["spacer"]) {}
            players.map { player ->
                keyedPlayerCard(player, tribe)
            }
        }
        div(classes = styles["heatmapPlayersSideRow"]) {
            players.map { player ->
                keyedPlayerCard(player, tribe)
            }
        }
        heatmap(HeatmapProps(heatmapData, styles["heatmap"]))
    }
}

private fun RDOMBuilder<DIV>.keyedPlayerCard(player: Player, tribe: Tribe) = div(classes = styles["playerCard"]) {
    attrs { key = player.id ?: "" }
    playerCard(PlayerCardProps(tribe.id, player, size = 50))
}
