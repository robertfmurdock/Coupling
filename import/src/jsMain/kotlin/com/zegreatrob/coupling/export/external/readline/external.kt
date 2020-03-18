@file:JsModule("readline")
@file:JsNonModule

package com.zegreatrob.coupling.export.external.readline

external fun createInterface(args: dynamic): ReadLine

external class ReadLine {
    fun on(eventName: String, handle: (String) -> Unit)
    fun close()
}
