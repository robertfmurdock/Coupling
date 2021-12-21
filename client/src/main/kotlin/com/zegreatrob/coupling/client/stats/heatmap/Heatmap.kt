package com.zegreatrob.coupling.client.stats.heatmap

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.height
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.classes
import org.w3c.dom.Node
import react.dom.attrs
import react.useLayoutEffect
import react.useRef
import styled.css
import styled.styledDiv

@JsModule("com/zegreatrob/coupling/client/components/heatmap/D3Heatmap")
external val d3Heatmap: dynamic

data class Heatmap(val data: List<List<Double?>>, val className: String) : DataProps<Heatmap> {
    override val component: TMFC<Heatmap> get() = heatmap
}

private val styles = useStyles("stats/heatmap/Heatmap")

val heatmap = reactFunction<Heatmap> { (data, className) ->
    val rowSize = data.size * 90
    val rootRef = useRef<Node>(null)
    useLayoutEffect { rootRef.current?.renderD3Heatmap(data.flatten()) }

    styledDiv {
        attrs { ref = rootRef; classes = classes + styles.className + className }
        css {
            width = rowSize.px
            height = rowSize.px
        }
    }
}

private fun Node.renderD3Heatmap(flatten: List<Double?>) {
    d3Heatmap.renderD3Heatmap(this, flatten.toTypedArray(), styles["cell"])
}
