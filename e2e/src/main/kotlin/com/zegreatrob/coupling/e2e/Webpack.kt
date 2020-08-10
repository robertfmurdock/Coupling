package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.webpack.WebpackCompiler
import com.zegreatrob.coupling.e2e.external.webpack.WebpackConfig
import com.zegreatrob.coupling.e2e.external.webpack.WebpackStats
import com.zegreatrob.coupling.e2e.external.webpack.webpack
import kotlinx.coroutines.CompletableDeferred

suspend fun runWebpack(config: WebpackConfig) = webpack(config)
    .runAsync()
    .await()
    .let { stats -> console.log(stats.toString("minimal")) }

private fun WebpackCompiler.runAsync() = CompletableDeferred<WebpackStats>().also {
    run { err: Throwable?, stats ->
        if (err != null)
            it.completeExceptionally(err)
        else
            it.complete(stats)
    }
}

fun webpackConfig() = kotlinext.js.require("webpack.config.js").unsafeCast<WebpackConfig>()
