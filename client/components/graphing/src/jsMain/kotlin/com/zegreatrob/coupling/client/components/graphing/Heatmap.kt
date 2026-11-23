package com.zegreatrob.coupling.client.components.graphing

import com.zegreatrob.coupling.client.components.graphing.external.d3.color.rgb
import com.zegreatrob.coupling.client.components.graphing.external.d3.interpolate.interpolateRgbBasis
import com.zegreatrob.coupling.client.components.graphing.external.d3.selection.select
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import react.Props
import react.dom.html.ReactHTML.div
import react.useLayoutEffect
import react.useRef
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.Normal
import web.cssom.px
import web.html.HTMLElement

external interface D3Thing {
    fun selectAll(elementType: String): D3Thing
    fun data(data: Array<HeatmapCell>): D3Thing
    fun join(elementType: String): D3Thing
    fun attr(name: String, value: String): D3Thing
    fun attr(name: String, value: (HeatmapCell) -> String?): D3Thing
    fun style(styleName: String, block: (HeatmapCell) -> String): D3Thing
}

data class HeatmapCell(val value: Double?, val description: String? = null, val outlined: Boolean = false)

val colorSuggestions = arrayOf(
    rgb("#2c7bb6"),
    rgb("#00a6ca"),
    rgb("#00ccbc"),
    rgb("#90eb9d"),
    rgb("#ffff8c"),
    rgb("#f9d057"),
    rgb("#f29e2e"),
    rgb("#e76818"),
    rgb("#d7191c"),
)

val interpolatorAsync = MainScope().async { makeInterpolator() }

private fun makeInterpolator() = interpolateRgbBasis(colorSuggestions)

suspend fun renderD3Heatmap(element: HTMLElement, data: List<HeatmapCell>, cellClassName: ClassName) {
    val colorInterpolator = interpolatorAsync.await()
    select(element)
        .selectAll("div")
        .data(data.toTypedArray())
        .join("div")
        .attr("class", "$cellClassName")
        .attr("title") { it.description }
        .attr("role") { if (it.description == null) null else "img" }
        .attr("aria-label") { it.description }
        .style("outline") { if (it.outlined) "2px solid #000" else "none" }
        .style("outline-offset") { "-2px" }
        .style("background-color") { cell ->
            if (cell.value == null) {
                "#EEE"
            } else {
                colorInterpolator(cell.value / 10)
            }
        }
}

external interface HeatmapProps : Props {
    var data: List<List<HeatmapCell>>
    var className: ClassName
}

@ReactFunc
val Heatmap by nfc<HeatmapProps> { (data, className) ->
    val rowSize = data.size * 90
    val rootRef = useRef<HTMLElement>(null)
    useLayoutEffect {
        rootRef.current?.renderD3Heatmap(data.flatten())
    }
    div {
        asDynamic()["data-heatmap"] = data.joinToString(",") { "[${it.joinToString(",") { cell -> cell.value.toString() }}]" }
        css(className) {
            lineHeight = 0.px
            width = rowSize.px
            height = rowSize.px
            whiteSpace = Normal.normal
        }
        ref = rootRef
    }
}

private suspend fun HTMLElement.renderD3Heatmap(flatten: List<HeatmapCell>) {
    renderD3Heatmap(
        this,
        flatten,
        ClassName {
            height = 90.px
            width = 90.px
            display = Display.inlineBlock
        },
    )
}
