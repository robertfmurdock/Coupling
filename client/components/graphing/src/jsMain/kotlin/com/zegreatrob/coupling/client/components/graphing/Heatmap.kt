package com.zegreatrob.coupling.client.components.graphing

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import js.import.importAsync
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

val d3Selection = importAsync<D3Selection>("d3-selection")
val d3Interpolate = importAsync<D3Interpolate>("d3-interpolate")
val d3Color = importAsync<D3Color>("d3-color")

external interface D3Color {
    fun rgb(value: String): String
}

external interface D3Interpolate {
    fun interpolateRgbBasis(colors: Array<String>): (Number) -> String
}

external interface D3Selection {
    fun select(element: HTMLElement): D3Thing
}

external interface D3Thing {
    fun selectAll(elementType: String): D3Thing
    fun data(data: Array<Double?>): D3Thing
    fun enter(): D3Thing
    fun append(block: () -> Element): D3Thing
    fun style(styleName: String, block: (Double?) -> String)
}

val colorSuggestions = d3Color.then {
    arrayOf(
        it.rgb("#2c7bb6"),
        it.rgb("#00a6ca"),
        it.rgb("#00ccbc"),
        it.rgb("#90eb9d"),
        it.rgb("#ffff8c"),
        it.rgb("#f9d057"),
        it.rgb("#f29e2e"),
        it.rgb("#e76818"),
        it.rgb("#d7191c"),
    )
}

val interpolatorAsync = MainScope().async { makeInterpolator() }

private suspend fun makeInterpolator() = d3Interpolate.await().interpolateRgbBasis(colorSuggestions.await())

suspend fun renderD3Heatmap(element: HTMLElement, data: List<Double?>, cellClassName: ClassName) {
    val colorInterpolator = interpolatorAsync.await()
    d3Selection.await().select(element)
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
