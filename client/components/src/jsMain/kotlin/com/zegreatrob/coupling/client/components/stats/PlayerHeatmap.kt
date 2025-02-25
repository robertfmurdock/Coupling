package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.CouplingHeatmapTooltip
import com.zegreatrob.coupling.client.components.PairTickMark
import com.zegreatrob.coupling.client.components.colorContext
import com.zegreatrob.coupling.client.components.external.nivo.colors.useOrdinalColorScale
import com.zegreatrob.coupling.client.components.external.nivo.heatmap.ResponsiveHeatMap
import com.zegreatrob.coupling.client.components.graphing.Heatmap
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.graphing.interpolatorAsync
import com.zegreatrob.coupling.client.components.pairContext
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import js.objects.jso
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useMemo
import react.useState
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
        players.forEach { player -> TopRowPlayer(player) }
    }
    div {
        className = heatmapSideRow
        players.forEach { player -> SidePlayer(player) }
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

@ReactFunc
val PlayerHeatmap2 by nfc<PlayerHeatmapProps> { (players, heatmapData) ->
    val (interpolator, setInterpolator) = useState<((Number) -> String)?>(null)
    useEffect {
        val value = interpolatorAsync.await()
        setInterpolator(transform = { value })
    }
    interpolator ?: return@nfc
    val getColor = useOrdinalColorScale(jso { scheme = "pastel1" }, "value")
    val max = 10
    div {
        colorContext.Provider {
            this.value = getColor
            pairContext {
                this.value = players.map { pairOf(it) }.toSet()
                ResponsiveHeatMap {
                    legend = "Pair Commits"
                    this.data = emptyArray()
                    colors = { datum -> interpolator(datum.value.toDouble() / max) }
                    emptyColor = interpolator(0)
                    margin = NivoChartMargin(
                        top = 65,
                        right = 90,
                        bottom = 60,
                        left = 90,
                    )
                    tooltip = CouplingHeatmapTooltip
                    axisLeft = NivoAxis(
                        tickSize = 5,
                        tickPadding = 5,
                        legendOffset = -52,
                        tickRotation = 90,
                        truncateTickAt = 0,
                        ticksPosition = "before",
                        renderTick = PairTickMark,
                    )
                    axisTop = NivoAxis(
                        tickSize = 5,
                        tickPadding = 5,
                        tickRotation = 180,
                        legendOffset = -30,
                        truncateTickAt = 0,
                        renderTick = PairTickMark,
                    )
                    axisRight = NivoAxis(
                        tickSize = 5,
                        tickPadding = 5,
                        tickRotation = -90,
                        legend = "",
                        legendPosition = "middle",
                        legendOffset = 70,
                        truncateTickAt = 0,
                        renderTick = PairTickMark,
                    )
                    axisBottom = NivoAxis(
                        renderTick = PairTickMark,
                        tickRotation = 0,
                    )
                }
            }
        }
    }
}
