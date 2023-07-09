package com.zegreatrob.coupling.client

fun isTestRun() = arrayOf("afterEach", "after", "beforeEach", "before", "describe", "it").all {
    js("global")[it] is Function<*>
}
