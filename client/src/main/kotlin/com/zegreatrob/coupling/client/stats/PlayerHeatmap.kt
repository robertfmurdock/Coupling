package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.stats.heatmap.Heatmap
import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Display
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.number
import csstype.px
import emotion.css.ClassName
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div

data class PlayerHeatmap(
    val players: List<Player>,
    val heatmapData: List<List<Double?>>,
) :
    DataPropsBind<PlayerHeatmap>(playerHeatmap)

val heatmapTopRowClass = ClassName {
}

val heatmapSideRow = ClassName {
    display = Display.inlineBlock
}

val playerHeatmap = tmFC<PlayerHeatmap> { (players, heatmapData) ->
    ReactHTML.div {
        css {
            display = Display.inlineBlock
            verticalAlign = VerticalAlign.top
            marginLeft = 20.px
            whiteSpace = csstype.WhiteSpace.nowrap
            flexShrink = number(0.0)
        }
        div {
            className = heatmapTopRowClass
            div {
                css {
                    display = Display.inlineBlock
                    width = 62.px
                }
            }
            players.map { player ->
                keyedPlayerCard(player, true)
            }
        }
        div {
            className = heatmapSideRow
            players.map { player ->
                keyedPlayerCard(player, false)
            }
        }
        add(
            Heatmap(
                heatmapData,
                ClassName {
                    display = Display.inlineBlock
                    verticalAlign = VerticalAlign.top
                }
            )
        )
    }
}

private fun ChildrenBuilder.keyedPlayerCard(player: Player, topRow: Boolean) = div {
    css {
        if (topRow) {
            display = Display.inlineBlock
            width = 90.px
            textAlign = TextAlign.center
        } else {
            display = Display.block
            height = 90.px
            "> div" {
                verticalAlign = VerticalAlign.middle
            }
        }
    }
    key = player.id
    add(PlayerCard(player, size = 50))
}
