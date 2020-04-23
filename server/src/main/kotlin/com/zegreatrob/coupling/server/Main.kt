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

    configureExpress(app)
    configureRoutes(expressWs)

    val startDeferred = CompletableDeferred<Unit>()

    val port = app.get("port").unsafeCast<Int?>() ?: 0
    app.listen(port) {
        logStartup(port, Config.buildDate, Config.gitRev, app.get("env").unsafeCast<String?>() ?: "UNKNOWN")
        startDeferred.complete(Unit)
    }

    startDeferred.await()
}

fun main() {
//    MainScope().launch { startDeferred.await() }
}
