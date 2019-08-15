package com.zegreatrob.coupling.client.stats.heatmap

import com.zegreatrob.coupling.client.external.react.*
import kotlinx.css.height
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.ReactElement
import styled.css
import styled.styledDiv

@JsModule("com/zegreatrob/coupling/client/components/heatmap/D3Heatmap")
external val d3Heatmap: dynamic

object Heatmap : ComponentProvider<HeatmapProps>(provider()), HeatmapBuilder

val RBuilder.heatmap get() = Heatmap.render(this)

data class HeatmapProps(val data: List<List<Double?>>, val className: String) : RProps

external interface HeatmapStyles {
    val className: String
    val cell: String
}

interface HeatmapBuilder : StyledComponentRenderer<HeatmapProps, HeatmapStyles> {

    override val componentPath: String get() = "stats/heatmap/Heatmap"

    override fun StyledRContext<HeatmapProps, HeatmapStyles>.render(): ReactElement {
        val rowSize = props.data.size * 90
        val rootRef = useRef<Node>(null)
        useLayoutEffect { rootRef.current?.renderD3Heatmap(props.data.flatten(), styles) }

        return reactElement {
            styledDiv {
                attrs { ref = rootRef; classes += styles.className; classes += props.className }
                css {
                    width = rowSize.px
                    height = rowSize.px
                }
            }
        }
    }

    private fun Node.renderD3Heatmap(flatten: List<Double?>, styles: HeatmapStyles) {
        d3Heatmap.renderD3Heatmap(this, flatten.toTypedArray(), styles.cell)
    }

}

