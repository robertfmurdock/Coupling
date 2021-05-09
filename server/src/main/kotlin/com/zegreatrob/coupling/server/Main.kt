package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.express.middleware.middleware
import com.zegreatrob.coupling.server.express.port
import com.zegreatrob.coupling.server.express.route.routes
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.express
import com.zegreatrob.coupling.server.external.expressws.expressWs
import kotlinx.coroutines.*

val serverScope = MainScope() + CoroutineName("Server")

@JsName("start")
fun start() = startDeferred.asPromise()

private val startDeferred = serverScope.async(start = CoroutineStart.LAZY) {
    val expressWs = expressWs(express())
    val app = expressWs.app

    app.middleware()
    expressWs.routes()

    app.startListening()
    Process.send("ready")
}

private suspend fun Express.startListening() = CompletableDeferred<Unit>()
    .apply {
        listen(port) {
            logStartup(port, Config.buildDate, Config.gitRev, env)
            complete(Unit)
        }
    }.await()

fun main() {
    serverScope.launch { startDeferred.await() }
}
