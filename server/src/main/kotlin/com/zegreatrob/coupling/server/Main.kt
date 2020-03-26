package com.zegreatrob.coupling.server

import kotlin.js.Promise

@JsName("start")
fun start(): Promise<Unit> {
    return js("require('./app.ts').start()")
}

fun main() {
//    MainScope().launch {
//        start().await()
//    }
}