package com.zegreatrob.coupling.client

fun main() {
    js("require('prefixfree')")
    js("require('com/zegreatrob/coupling/client/style.scss')")
    js("require('com/zegreatrob/coupling/client/animations.scss')")
    App.bootstrapApp()
}