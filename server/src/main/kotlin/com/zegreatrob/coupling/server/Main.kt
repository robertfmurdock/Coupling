package com.zegreatrob.coupling.server

import kotlinx.coroutines.*

@JsName("start")
fun start() = startDeferred.asPromise()

private val startDeferred = MainScope().async(start = CoroutineStart.LAZY) {
    println("KT Start.")
}

fun main() {
    MainScope().launch {
        startDeferred.await()
    }
}