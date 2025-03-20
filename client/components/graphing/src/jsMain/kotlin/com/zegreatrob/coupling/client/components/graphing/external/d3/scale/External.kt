@file:JsModule("d3-scale")

package com.zegreatrob.coupling.client.components.graphing.external.d3.scale

import js.date.Date
import seskar.js.JsNativeInvoke

external fun scaleOrdinal(): Scale
external fun scaleTime(): TimeScale

external interface Scale {
    fun domain(domain: Array<*>): Scale
    fun range(range: Array<*>): Scale

    @JsNativeInvoke
    operator fun invoke(value: Any): Any
}

external interface TimeScale {
    fun domain(domain: Array<Double>): TimeScale
    fun domain(): Array<Date>
    fun ticks(count: Number): Array<Date>
    fun range(range: Array<Any>): TimeScale
    fun nice(): TimeScale

    @JsNativeInvoke
    operator fun invoke(value: Any): Any
}
