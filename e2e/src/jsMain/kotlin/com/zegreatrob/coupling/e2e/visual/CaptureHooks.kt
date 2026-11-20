package com.zegreatrob.coupling.e2e.visual

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

typealias CapturePrepareHook = (CaptureWdioConfig, Any?) -> Any?
typealias CaptureCompleteHook = (Int, CaptureWdioConfig, Any?, Any?) -> Any?

@JsPlainObject
external interface CaptureWdioConfig {
    var mochaOpts: CaptureMochaOptions
    var onPrepare: CapturePrepareHook?
    var onComplete: CaptureCompleteHook?
}

@JsPlainObject
external interface CaptureMochaOptions {
    var fgrep: String?
    var timeout: Int?
}

// Copy the open-ended Mocha options without discarding options outside our binding.
@JsName("Object")
private external object NativeObject {
    fun assign(target: CaptureMochaOptions, source: CaptureMochaOptions, overrides: CaptureMochaOptions): CaptureMochaOptions
}

internal fun configureCaptureHooks(
    config: CaptureWdioConfig,
    begin: suspend () -> Unit,
    finish: suspend (Int) -> Boolean,
): CaptureWdioConfig {
    val prepare = config.onPrepare
    val complete = config.onComplete
    config.mochaOpts = NativeObject.assign(
        CaptureMochaOptions(),
        config.mochaOpts,
        CaptureMochaOptions(fgrep = js("undefined"), timeout = 120000),
    )
    config.onPrepare = { options, capabilities ->
        MainScope().promise {
            begin()
            prepare?.let { Promise.resolve(it(options, capabilities)).await() }
            Unit
        }
    }
    config.onComplete = { exitCode, options, capabilities, results ->
        MainScope().promise {
            try {
                complete?.let { Promise.resolve(it(exitCode, options, capabilities, results)).await() }
            } catch (failure: Throwable) {
                finish(1)
                throw failure
            }
            check(finish(exitCode)) { "Visual evidence did not complete; inspect manifest" }
        }
    }
    return config
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object CaptureHooks {
    fun configure(config: CaptureWdioConfig): CaptureWdioConfig = configureCaptureHooks(
        config,
        begin = {
            val manifest = CaptureLifecycle.begin().await()
            setCaptureEnv("COUPLING_CAPTURE_MANIFEST", manifest)
            println("Visual evidence manifest: $manifest")
        },
        finish = { exitCode -> CaptureLifecycle.finish(captureEnv("COUPLING_CAPTURE_MANIFEST"), exitCode).await() },
    )
}
