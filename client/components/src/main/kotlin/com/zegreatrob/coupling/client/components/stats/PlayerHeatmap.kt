package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.PlayerCard
import com.zegreatrob.coupling.client.components.stats.heatmap.Heatmap
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import csstype.Display
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.WhiteSpace
import csstype.deg
import csstype.number
import csstype.px
import emotion.css.ClassName
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.useMemo
import kotlin.random.Random

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

val playerHeatmap by ntmFC<PlayerHeatmap> { (players, heatmapData) ->
    div {
        css {
            display = Display.inlineBlock
            verticalAlign = VerticalAlign.top
            marginLeft = 20.px
            whiteSpace = WhiteSpace.nowrap
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
            players.forEach { player -> add(TopRowPlayer(player)) }
        }
        div {
            className = heatmapSideRow
            players.forEach { player -> add(SidePlayer(player)) }
        }
        add(
            Heatmap(
                heatmapData,
                ClassName {
                    display = Display.inlineBlock
                    verticalAlign = VerticalAlign.top
                },
            ),
        )
    }
}

val topRowPlayer by ntmFC<TopRowPlayer> { props ->
    val tweak = useMemo { Random.nextInt(6).toDouble() - 3.0 }
    div {
        css {
            display = Display.inlineBlock
            width = 90.px
            textAlign = TextAlign.center
        }
        key = props.player.id
        add(PlayerCard(props.player, size = 50, tilt = tweak.deg))
    }
}

data class TopRowPlayer(val player: Player) : DataPropsBind<TopRowPlayer>(topRowPlayer)

val sidePlayer by ntmFC<SidePlayer> { props ->
    val tweak = useMemo(props.player.id) { 1.5 - Random.nextInt(6).toDouble() }
    div {
        css {
            display = Display.block
            height = 90.px
            "> div" {
                verticalAlign = VerticalAlign.middle
            }
        }
        key = props.player.id
        add(PlayerCard(props.player, size = 50, tilt = tweak.deg))
    }
}

data class SidePlayer(val player: Player) : DataPropsBind<SidePlayer>(sidePlayer)
