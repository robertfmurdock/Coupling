package com.zegreatrob.coupling.server

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

@JsName("start")
fun start(): Promise<Unit> {
    return js("require('./app.ts').start()")
}

fun main() {
    MainScope().launch {
        start().await()
    }
}