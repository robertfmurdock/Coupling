package com.zegreatrob.coupling.client.stats.heatmap

import com.zegreatrob.coupling.client.external.react.childFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.height
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.useLayoutEffect
import react.useRef
import styled.css
import styled.styledDiv

@JsModule("com/zegreatrob/coupling/client/components/heatmap/D3Heatmap")
external val d3Heatmap: dynamic

data class HeatmapProps(val data: List<List<Double?>>, val className: String) : RProps

private val styles = useStyles("stats/heatmap/Heatmap")

val Heatmap =
    reactFunction<HeatmapProps> { (data, className) ->
        val rowSize = data.size * 90
        val rootRef = useRef<Node?>(null)
        useLayoutEffect { rootRef.current?.renderD3Heatmap(data.flatten()) }

        styledDiv {
            attrs { ref = rootRef; classes += styles.className; classes += className }
            css {
                width = rowSize.px
                height = rowSize.px
            }
        }
    }

private fun Node.renderD3Heatmap(flatten: List<Double?>) {
    d3Heatmap.renderD3Heatmap(this, flatten.toTypedArray(), styles["cell"])
}
