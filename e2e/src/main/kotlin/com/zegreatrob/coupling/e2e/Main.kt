package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.fsextras.removeDirectory
import com.zegreatrob.coupling.e2e.external.webpack.WebpackConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun main() {
    js("process.env.PORT = 3001")
    val config = webpackConfig()

    console.log("launch")
    MainScope().launch {
        val serverProcess = runWebpackAndStartServer(config)
        val result = try {
            console.log("Starting tests.")
            runWebdriverIO("${config.output.path}/config.js")
        } catch (error: Throwable) {
            reportError(error)
        } finally {
            removeDirectory(config.output.path)
            serverProcess.kill()
        }
        process.exit(result)
    }.invokeOnCompletion { huh ->
        if (huh != null) {
            process.exit(reportError(huh))
        }
    }
}

private suspend fun runWebpackAndStartServer(config: WebpackConfig) = coroutineScope {
    launch {
        runWebpack(config)
    }
    startServer()
}

private fun reportError(error: Throwable) = (-1).also { console.log("Error", error) }

