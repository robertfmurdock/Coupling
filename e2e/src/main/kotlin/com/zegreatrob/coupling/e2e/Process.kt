@file:Suppress("unused")

package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.childprocess.Writable
import kotlin.js.Json

external val process: Process

external interface Process {

    val env: Json
    val stdin: Writable

    fun exit()
    fun exit(code: Int)
}
