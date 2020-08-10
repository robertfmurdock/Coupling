package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.childprocess.ChildProcess
import com.zegreatrob.coupling.e2e.external.fsextras.removeDirectory
import com.zegreatrob.coupling.e2e.external.webpack.WebpackConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun main() {
    js("process.env.PORT = 3001")
    val config = webpackConfig()

    MainScope().launch {
        runWebpackAndStartServer(config)
            .whileRunning { runWebdriverIO(config.wdioConfig()) }
            .let { result -> process.exit(result) }
    }.invokeOnCompletion { huh ->
        if (huh != null)
            process.exit(reportError(huh))
    }
}

private fun WebpackConfig.wdioConfig() = "${output.path}/config.js"

private suspend fun runWebpackAndStartServer(config: WebpackConfig) = coroutineScope {
    launch { runWebpack(config) }
    startServer()
}.let { ServerWithWebpackDisposable(it, config) }

class ServerWithWebpackDisposable(private val process: ChildProcess, private val config: WebpackConfig) {
    suspend fun <T> whileRunning(callback: suspend () -> T) = try {
        callback()
    } finally {
        removeDirectory(config.output.path)
        process.kill()
    }
}

private fun reportError(error: Throwable) = (-1).also { console.log("Error", error) }
