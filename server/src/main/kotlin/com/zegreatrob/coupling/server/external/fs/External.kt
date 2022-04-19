@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.fs

@JsModule("fs")
external val fs: Filesystem

external interface Filesystem {
    fun readFileSync(path: String, encoding: String): String
    fun createReadStream(resourcePath: String): Stream
}

external interface Stream {
    fun pipe(stream: Any): Stream
}
