package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.fsextras.removeDirectory
import com.zegreatrob.coupling.e2e.external.webpack.WebpackConfig
import com.zegreatrob.coupling.e2e.external.webpack.runWebpack
import com.zegreatrob.coupling.e2e.external.webpack.webpackConfig
import com.zegreatrob.wrapper.wdio.cli.runWebdriverIO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun main() {
    val config = webpackConfig()

    MainScope().launch {
        runWebpackAndStartServer(config)
            .whileRunning { runWebdriverIO(wdioConfig()) }
            .let { result -> process.exit(result) }
    }.invokeOnCompletion { huh ->
        if (huh != null) {
            process.exit(reportError(huh))
        }
    }
}

private fun webpackConfig() = webpackConfig(process.envString("WEBPACK_CONFIG"))

private fun Process.envString(key: String) = env[key].unsafeCast<String>()

private fun wdioConfig() = process.envString("WDIO_CONFIG")

private suspend fun runWebpackAndStartServer(config: WebpackConfig) = coroutineScope {
    launch { runWebpack(config) }
}.let { ServerWithWebpackDisposable(config) }

class ServerWithWebpackDisposable(private val config: WebpackConfig) {
    suspend fun <T> whileRunning(callback: suspend () -> T) = try {
        callback()
    } finally {
        removeDirectory(config.output.path)
    }
}

private fun reportError(error: Throwable) = (-1).also { console.log("Error", error) }
