package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.childprocess.Writable


external val process: Process

external interface Process {

    val env: dynamic
    val stdin: Writable

    fun exit()
    fun exit(code: Int)

}
