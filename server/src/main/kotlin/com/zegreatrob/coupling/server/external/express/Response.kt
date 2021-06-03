@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.express

import com.zegreatrob.coupling.server.external.fs.Stream
import kotlin.js.Json

external interface Response {
    var statusCode: Int

    operator fun get(key: String): Any?

    fun send(body: Json?)
    fun send(body: Array<*>)
    fun send(body: Any?)

    fun sendStatus(statusCode: Int)
    fun redirect(path: String)
    fun render(view: String, json: Json)
    fun pipe(stream: Stream): Pipe
    fun setEncoding(encoding: String)
    fun type(type: String)
}

external interface Pipe {
    fun pipe(stream: Stream): Pipe
}
