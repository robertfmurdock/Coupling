package com.zegreatrob.coupling.e2e.external.webpack

import kotlinx.coroutines.CompletableDeferred

suspend fun runWebpack(config: WebpackConfig, statsOutput: String = "minimal") = webpack(config)
    .runAsync()
    .await()
    .let { stats -> console.log(stats.toString(statsOutput)) }

private fun WebpackCompiler.runAsync() = CompletableDeferred<WebpackStats>().also {
    run { err: Throwable?, stats ->
        if (err != null) {
            it.completeExceptionally(err)
        } else {
            it.complete(stats)
        }
    }
}

fun webpackConfig(webpackModulePath: String) = kotlinext.js.require(webpackModulePath)
    .unsafeCast<WebpackConfig>()
