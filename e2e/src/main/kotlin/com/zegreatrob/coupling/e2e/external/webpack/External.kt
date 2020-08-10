package com.zegreatrob.coupling.e2e.external.webpack

@JsModule("webpack")
external fun webpack(config: WebpackConfig): WebpackCompiler

external interface WebpackCompiler {
    fun run(callback: (err: dynamic, stats: WebpackStats) -> Unit)
}

external interface WebpackStats {
    val entries: dynamic
    fun toString(format: String): String
}

external interface WebpackConfig {
    val output: WebpackOutput
}

external interface WebpackOutput {
    val path: String
}
