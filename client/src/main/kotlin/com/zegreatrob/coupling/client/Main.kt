package com.zegreatrob.coupling.client

fun main() {
    js("require('prefixfree')")
    js("require('com/zegreatrob/coupling/client/animations.css')")
    js("require('@fortawesome/fontawesome-free/css/all.css')")
    App.bootstrapApp()
}
