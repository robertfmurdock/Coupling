package com.zegreatrob.coupling.client.components.graphing

import com.zegreatrob.coupling.client.components.graphing.external.d3.color.rgb
import com.zegreatrob.coupling.client.components.graphing.external.d3.interpolate.interpolateRgbBasis
import com.zegreatrob.coupling.client.components.graphing.external.d3.selection.select
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import org.w3c.dom.Element
import react.Props
import react.dom.html.ReactHTML.div
import react.useLayoutEffect
import react.useRef
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.WhiteSpace
import web.cssom.px
import web.html.HTMLElement

external interface D3Thing {
    fun selectAll(elementType: String): D3Thing
    fun data(data: Array<Double?>): D3Thing
    fun enter(): D3Thing
    fun append(block: () -> Element): D3Thing
    fun style(styleName: String, block: (Double?) -> String)
}

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

suspend fun renderD3Heatmap(element: HTMLElement, data: List<Double?>, cellClassName: ClassName) {
    val colorInterpolator = interpolatorAsync.await()
    select(element)
        .selectAll("div")
        .data(data.toTypedArray())
        .enter()
        .append {
            document.createElement("div")
                .also { it.setAttribute("class", "$cellClassName") }
        }
        .style("background-color") { dataNumber ->
            if (dataNumber == null) {
                "#EEE"
            } else {
                colorInterpolator(dataNumber / 10)
            }
        }
}

external interface HeatmapProps : Props {
    var data: List<List<Double?>>
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
        asDynamic()["data-heatmap"] = data.joinToString(",") { "[${it.joinToString(",")}]" }
        css(className) {
            lineHeight = 0.px
            width = rowSize.px
            height = rowSize.px
            whiteSpace = WhiteSpace.normal
        }
        ref = rootRef
    }
}

private suspend fun HTMLElement.renderD3Heatmap(flatten: List<Double?>) {
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
