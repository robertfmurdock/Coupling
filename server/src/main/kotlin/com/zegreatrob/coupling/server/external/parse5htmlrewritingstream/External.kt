package com.zegreatrob.coupling.server.external.parse5htmlrewritingstream

import com.zegreatrob.coupling.server.external.fs.Stream

@JsModule("parse5-html-rewriting-stream")
external class RewritingStream : Stream {
    fun on(event: String, callback: (tag: Tag) -> Unit)
    fun on(event: String, callback: (tag: Tag, String) -> Unit)
    fun emitStartTag(tag: Tag)
    fun emitRaw(raw: String)
    fun emitEndTag(tag: Tag)

    override fun pipe(stream: Any): Stream
}

external interface Tag {
    val tagName: String
}
