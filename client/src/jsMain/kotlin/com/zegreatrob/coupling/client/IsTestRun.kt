package com.zegreatrob.coupling.client

import js.globals.globalThis

fun isTestRun() = arrayOf("afterEach", "after", "beforeEach", "before", "describe", "it").all {
    globalThis[it] is Function<*>
}
