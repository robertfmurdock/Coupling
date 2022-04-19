@file:JsModule("readline")
@file:Suppress("unused")

package com.zegreatrob.coupling.import.external.readline

external fun createInterface(args: dynamic): ReadLine

external class ReadLine {
    fun on(eventName: String, handle: (String) -> Unit)
    fun close()
}
