package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.stats.heatmap.Heatmap
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import emotion.css.ClassName
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.useMemo
import web.cssom.Display
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.deg
import web.cssom.number
import web.cssom.px
import kotlin.random.Random

val heatmapTopRowClass = ClassName {
}

val heatmapSideRow = ClassName {
    display = Display.inlineBlock
}

external interface PlayerHeatmapProps : Props {
    var players: List<Player>
    var heatmapData: List<List<Double?>>
}

@ReactFunc
val PlayerHeatmap by nfc<PlayerHeatmapProps> { (players, heatmapData) ->
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
        PlayerCard(props.player, size = 50, tilt = tweak.deg)
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
        PlayerCard(props.player, size = 50, tilt = tweak.deg)
    }
}

data class SidePlayer(val player: Player) : DataPropsBind<SidePlayer>(sidePlayer)
