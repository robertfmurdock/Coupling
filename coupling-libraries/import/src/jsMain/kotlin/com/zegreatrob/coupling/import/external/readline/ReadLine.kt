package com.zegreatrob.coupling.import.external.readline

external class ReadLine {
    fun on(eventName: String, handle: (String) -> Unit)
    fun close()
}
