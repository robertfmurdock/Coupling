package com.zegreatrob.coupling.client.components.graphing.external.d3.array

@JsModule("d3-array")
external val d3Array: D3Array

external interface D3Array {
    fun <T> quantileSorted(array: Array<T>, p: Double): T where T : Number
}
