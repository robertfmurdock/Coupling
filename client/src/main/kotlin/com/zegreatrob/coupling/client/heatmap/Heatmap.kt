package com.zegreatrob.coupling.client.heatmap

import com.zegreatrob.coupling.client.*
import kotlinx.css.height
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.RReadableRef
import styled.css
import styled.styledDiv

@JsModule("components/heatmap/D3Heatmap")
@JsNonModule
external val d3Heatmap: dynamic

object Heatmap : ComponentProvider<HeatmapProps>(), HeatmapBuilder

val RBuilder.heatmap get() = Heatmap.captor(this)

data class HeatmapProps(val data: List<List<Double?>>, val className: String) : RProps

external interface HeatmapStyles {
    val className: String
    val cell: String
}

interface HeatmapBuilder : StyledComponentBuilder<HeatmapProps, HeatmapStyles> {

    override val componentPath: String get() = "heatmap/Heatmap"

    override fun build() = buildBy {
        val rowSize = props.data.size * 90
        {
            val rootRef: RReadableRef<Node> = useRef(null)

            useLayoutEffect { rootRef.current?.renderD3Heatmap(props.data.flatten(), styles) }

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

