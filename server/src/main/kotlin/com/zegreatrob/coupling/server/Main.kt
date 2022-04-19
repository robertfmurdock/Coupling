package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.express.middleware.middleware
import com.zegreatrob.coupling.server.express.port
import com.zegreatrob.coupling.server.express.route.routes
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.express
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

val serverScope = MainScope() + CoroutineName("Server")

@JsName("start")
fun start() = startDeferred.asPromise()

private val startDeferred = serverScope.async(start = CoroutineStart.LAZY) {
    buildApp()
        .startListening()
    Process.send("ready")
}

fun buildApp(): Express {
    val app = express()
    app.middleware()
    app.routes()
    return app
}

private suspend fun Express.startListening() = CompletableDeferred<Unit>()
    .apply {
        listen(port) {
            logStartup(port, Config.buildDate, Config.gitRev, env)
            complete(Unit)
        }
    }.await()

fun main() {
    if (Process.getEnv("IS_OFFLINE") != "true" &&
        Process.getEnv("AWS_LAMBDA_FUNCTION_NAME").unsafeCast<String?>() == null
    ) {
        serverScope.launch { startDeferred.await() }
    }
}
