package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.graphing.Heatmap
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.useMemo
import web.cssom.Display
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.deg
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
        className = heatmapTopRowClass
        div {
            css {
                display = Display.inlineBlock
                width = 62.px
            }
        }
        players.forEach { player -> TopRowPlayer(player, key = player.id) }
    }
    div {
        className = heatmapSideRow
        players.forEach { player -> SidePlayer(player, key = player.id) }
    }
    Heatmap(
        heatmapData,
        ClassName {
            display = Display.inlineBlock
            verticalAlign = VerticalAlign.top
        },
    )
}

external interface TopRowPlayerProps : Props {
    var player: Player
}

@ReactFunc
val TopRowPlayer by nfc<TopRowPlayerProps> { props ->
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

external interface SidePlayerProps : Props {
    var player: Player
}

@ReactFunc
val SidePlayer by nfc<SidePlayerProps> { props ->
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
