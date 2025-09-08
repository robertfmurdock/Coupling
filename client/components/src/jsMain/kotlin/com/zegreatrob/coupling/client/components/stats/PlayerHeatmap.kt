package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.graphing.Heatmap
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML
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
    display = Display.Companion.inlineBlock
}

external interface PlayerHeatmapProps : Props {
    var players: List<Player>
    var heatmapData: List<List<Double?>>
}

@ReactFunc
val PlayerHeatmap by nfc<PlayerHeatmapProps> { (players, heatmapData) ->
    ReactHTML.div {
        className = heatmapTopRowClass
        ReactHTML.div {
            css {
                display = Display.Companion.inlineBlock
                width = 62.px
            }
        }
        players.forEach { player -> TopRowPlayer(player, key = player.id.value.toString()) }
    }
    ReactHTML.div {
        className = heatmapSideRow
        players.forEach { player -> SidePlayer(player, key = player.id.value.toString()) }
    }
    Heatmap(
        heatmapData,
        ClassName {
            display = Display.Companion.inlineBlock
            verticalAlign = VerticalAlign.Companion.top
        },
    )
}

external interface TopRowPlayerProps : Props {
    var player: Player
}

@ReactFunc
val TopRowPlayer by nfc<TopRowPlayerProps> { props ->
    val tweak = useMemo { Random.Default.nextInt(6).toDouble() - 3.0 }
    ReactHTML.div {
        css {
            display = Display.Companion.inlineBlock
            width = 90.px
            textAlign = TextAlign.Companion.center
        }
        key = props.player.id.value.toString()
        PlayerCard(props.player, size = 50, tilt = tweak.deg)
    }
}

external interface SidePlayerProps : Props {
    var player: Player
}

@ReactFunc
val SidePlayer by nfc<SidePlayerProps> { props ->
    val tweak = useMemo(props.player.id) { 1.5 - Random.Default.nextInt(6).toDouble() }
    ReactHTML.div {
        css {
            display = Display.Companion.block
            height = 90.px
            "> div" {
                verticalAlign = VerticalAlign.Companion.middle
            }
        }
        key = props.player.id.value.toString()
        PlayerCard(props.player, size = 50, tilt = tweak.deg)
    }
}
