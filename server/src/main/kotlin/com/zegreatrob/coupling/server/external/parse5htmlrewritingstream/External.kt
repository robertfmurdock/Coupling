@file:JsModule("parse5-html-rewriting-stream")

package com.zegreatrob.coupling.server.external.parse5htmlrewritingstream

import com.zegreatrob.coupling.server.external.fs.Stream

@JsName("RewritingStream")
external class RewritingStream : Stream {
    fun on(event: String, callback: (tag: Tag) -> Unit)
    fun on(event: String, callback: (tag: Tag, String) -> Unit)
    fun emitStartTag(tag: Tag)
    fun emitRaw(raw: String)
    fun emitEndTag(tag: Tag)

    override fun pipe(stream: Any): Stream
}

external interface Tag {
    var attrs: Array<Attribute>
    val tagName: String
}

external interface Attribute {
    val name: String
    val namespace: String
    var value: String
    val prefix: String
}
