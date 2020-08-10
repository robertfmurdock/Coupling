package com.zegreatrob.coupling.e2e.external.webpack

@JsModule("webpack")
external fun webpack(config: WebpackConfig): WebpackCompiler

external interface WebpackCompiler {
    fun run(callback: (err: dynamic, stats: dynamic) -> Unit)
}

external interface WebpackConfig {

}
