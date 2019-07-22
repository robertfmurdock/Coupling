package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.element
import react.RBuilder
import react.RClass
import react.RProps
import kotlin.js.json

@JsModule("components/heatmap/ReactHeatmap")
@JsNonModule
private external val reactHeatmapModule: dynamic

interface HeatmapSyntax {

    companion object {
        val rClass = reactHeatmapModule.default.unsafeCast<RClass<RProps>>()
    }

    fun RBuilder.heatmap(props: HeatmapProps) {
        element(
                rClass,
                json(
                        "className" to props.className.unsafeCast<String>(),
                        "data" to props.heatmapData.map { it.toTypedArray() }.toTypedArray()
                ).unsafeCast<RProps>()
        )
    }


}

data class HeatmapProps(val heatmapData: List<List<Double?>>, val className: String)