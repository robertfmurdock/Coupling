package com.zegreatrob.coupling.client.components.external.d3.array

import js.import.importAsync
import js.promise.asDeferred

val d3Array = importAsync<D3Array>("d3-array").asDeferred()

external interface D3Array {
    fun <T> quantileSorted(array: Array<T>, p: Double): T where T : Number
}
