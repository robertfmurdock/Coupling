package com.zegreatrob.coupling.client.stats.heatmap

import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Display
import csstype.WhiteSpace
import csstype.px
import dom.html.HTMLElement
import emotion.css.ClassName
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.useLayoutEffect
import react.useRef

@JsModule("com/zegreatrob/coupling/client/components/heatmap/D3Heatmap")
external val d3Heatmap: dynamic

data class Heatmap(val data: List<List<Double?>>, val className: ClassName) : DataPropsBind<Heatmap>(heatmap)

val heatmap = tmFC<Heatmap> { (data, className) ->
    val rowSize = data.size * 90
    val rootRef = useRef<HTMLElement>(null)
    useLayoutEffect { rootRef.current?.renderD3Heatmap(data.flatten()) }

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

private fun HTMLElement.renderD3Heatmap(flatten: List<Double?>) {
    d3Heatmap.renderD3Heatmap(
        this, flatten.toTypedArray(),
        ClassName {
            height = 90.px
            width = 90.px
            display = Display.inlineBlock
        }
    )
}
