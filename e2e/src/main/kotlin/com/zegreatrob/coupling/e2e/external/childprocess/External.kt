@file:JsModule("child_process")

package com.zegreatrob.coupling.e2e.external.childprocess

import kotlin.js.Json

external fun fork(module: String, args: Array<String>, options: Json): ChildProcess

external interface ChildProcess {
    val stdin: Writable
    val stdout: Writable
    val stderr: Writable

    fun on(event: String, function: (String) -> Unit)
    fun kill()
}

external interface Writable {
    fun pipe(stdin: Writable)
    fun on(s: String, any: (dynamic) -> Unit): Any
}
