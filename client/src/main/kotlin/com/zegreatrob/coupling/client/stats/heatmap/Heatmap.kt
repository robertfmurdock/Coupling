package com.zegreatrob.coupling.client.stats.heatmap

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import csstype.WhiteSpace
import csstype.px
import emotion.react.css
import org.w3c.dom.Node
import react.dom.html.ReactHTML.div
import react.useLayoutEffect
import react.useRef

@JsModule("com/zegreatrob/coupling/client/components/heatmap/D3Heatmap")
external val d3Heatmap: dynamic

data class Heatmap(val data: List<List<Double?>>, val className: String) : DataPropsBind<Heatmap>(heatmap)

private val styles = useStyles("stats/heatmap/Heatmap")

val heatmap = tmFC<Heatmap> { (data, className) ->
    val rowSize = data.size * 90
    val rootRef = useRef<Node>(null)
    useLayoutEffect { rootRef.current?.renderD3Heatmap(data.flatten()) }

    div {
        css(styles.className, csstype.ClassName(className)) {
            width = rowSize.px
            height = rowSize.px
            whiteSpace = WhiteSpace.normal
        }
        ref = rootRef
    }
}

private fun Node.renderD3Heatmap(flatten: List<Double?>) {
    d3Heatmap.renderD3Heatmap(this, flatten.toTypedArray(), styles["cell"])
}
