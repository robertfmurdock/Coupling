package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.StyledComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.client.stats.heatmap.HeatmapProps
import com.zegreatrob.coupling.client.stats.heatmap.heatmap
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.DIV
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.RDOMBuilder
import react.dom.div

object PlayerHeatmap : ComponentProvider<PlayerHeatmapProps>(), PlayerHeatmapBuilder

val RBuilder.playerHeatmap get() = PlayerHeatmap.captor(this)

data class PlayerHeatmapProps(
    val tribe: KtTribe,
    val players: List<Player>,
    val heatmapData: List<List<Double?>>
) : RProps

external interface PlayerHeatmapStyles {
    val rightSection: String
    val heatmapPlayersTopRow: String
    val spacer: String
    val playerCard: String
    val heatmapPlayersSideRow: String
    val heatmap: String
}

interface PlayerHeatmapBuilder : StyledComponentBuilder<PlayerHeatmapProps, PlayerHeatmapStyles> {

    override val componentPath get() = "stats/PlayerHeatmap"

    override fun build() = buildBy {
        {
            val tribe = props.tribe
            div(classes = styles.rightSection) {
                div(classes = styles.heatmapPlayersTopRow) {
                    div(classes = styles.spacer) {}
                    props.players.map { player ->
                        keyedPlayerCard(styles, player, tribe)
                    }
                }
                div(classes = styles.heatmapPlayersSideRow) {
                    props.players.map { player ->
                        keyedPlayerCard(styles, player, tribe)
                    }
                }
                heatmap(HeatmapProps(props.heatmapData, styles.heatmap))
            }
        }
    }

    private fun RDOMBuilder<DIV>.keyedPlayerCard(
        styles: PlayerHeatmapStyles,
        player: Player,
        tribe: KtTribe
    ): ReactElement {
        return div(classes = styles.playerCard) {
            attrs { key = player.id ?: "" }
            playerCard(PlayerCardProps(tribe.id, player, size = 50, pathSetter = {}))
        }
    }

}
