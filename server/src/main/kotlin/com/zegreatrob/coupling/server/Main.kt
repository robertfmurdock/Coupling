package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.external.express.express
import com.zegreatrob.coupling.server.external.expressws.expressWs
import kotlinx.coroutines.*

@JsName("start")
fun start() = startDeferred.asPromise()

private val startDeferred = MainScope().async(start = CoroutineStart.LAZY) {
    println("KT Start.")

    val expressWs = expressWs(express())
    val app = expressWs.app

    val couplingDataService = couplingDataService(Config.mongoUrl)
    val tempDataService = couplingDataService(Config.tempMongoUrl)

    app
}

fun main() {
    MainScope().launch {
//        startDeferred.await()
    }
}