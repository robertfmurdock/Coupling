@file:Suppress("unused", "unused_parameter")

package com.zegreatrob.coupling.e2e.diagnostics

import kotlin.js.Promise

// Load the same repository-owned ES module used directly by WebdriverIO.
internal fun <T> importModule(specifier: String): Promise<T> = js("import(specifier)")

@JsModule("node:fs/promises")
internal external object Files {
    fun mkdtemp(prefix: String): Promise<String>
    fun readFile(path: String, encoding: String = definedExternally): Promise<String>
    fun writeFile(path: String, data: String, options: dynamic = definedExternally): Promise<Unit>
    fun readdir(path: String): Promise<Array<String>>
    fun rm(path: String, options: dynamic): Promise<Unit>
    fun stat(path: String): Promise<FileStat>
}
internal external interface FileStat {
    val size: Double
}

@JsModule("node:os")
internal external object Os {
    fun tmpdir(): String
}

@JsModule("node:path")
internal external object Path {
    fun join(vararg parts: String): String
}

@JsModule("node:process")
internal external object ProcessModule {
    val default: NodeProcess
}
internal val nodeProcess: NodeProcess get() = ProcessModule.default
internal external interface NodeProcess {
    val env: Environment
    fun on(event: String, listener: (String) -> Unit)
    fun off(event: String, listener: (String) -> Unit)
}

internal external interface Environment {
    @JsName("COUPLING_DIAGNOSTICS_MODULE")
    val diagnosticsModule: String

    @JsName("COUPLING_REPOSITORY_ROOT")
    val repositoryRoot: String

    @JsName("WDIO_CHROME_BINARY")
    val chromeBinary: String?
}

@JsModule("node:test")
internal external object NodeTest {
    val mock: MockTracker
}
internal external interface MockTracker {
    val timers: MockTimers
}
internal external interface MockTimers {
    fun enable(options: dynamic)
    fun tick(milliseconds: Int)
    fun reset()
}

@JsModule("node:events")
internal external class EventEmitter {
    fun emit(event: String, value: dynamic = definedExternally): Boolean
    fun listenerCount(event: String): Int
    fun on(event: String, listener: () -> Unit): EventEmitter
    fun off(event: String, listener: () -> Unit): EventEmitter
}

internal external interface DiagnosticsModule {
    fun createCollector(browser: BrowserDouble, options: CollectorOptions = definedExternally): Collector
    fun configure(config: dynamic, directories: dynamic): RunnerConfig
}
internal external interface Collector {
    fun start(): Promise<Unit>
    fun stop(): Promise<Unit>
    fun drainLogs(): Promise<Array<dynamic>>
    fun captureFailure(error: dynamic): Promise<String>
    fun markBootstrapFailure(error: dynamic)
}
internal external interface RunnerConfig {
    fun before(): Promise<Unit>
    fun beforeTest(test: dynamic): Promise<Unit>
    fun afterTest(test: dynamic, context: dynamic, result: dynamic): Promise<Unit>
    fun after(): Promise<Unit>
}

// The test double deliberately models an open JS browser object. Keep casts and
// replaceable callbacks here rather than leaking dynamic browser access into tests.
internal external interface BrowserDouble {
    var isBidi: Boolean
    var sessionId: String
    var sessionSubscribe: (dynamic) -> Promise<Unit>
    var sessionUnsubscribe: (dynamic) -> Promise<Unit>
    var getLogs: () -> Promise<Array<dynamic>>
    var execute: () -> Promise<dynamic>
    var saveScreenshot: (String) -> Promise<Unit>
    var couplingDiagnostics: Collector
    fun emit(event: String, value: dynamic): Boolean
    fun listenerCount(event: String): Int
}

internal fun nativeError(message: String): dynamic = js("new Error(message)")
internal fun nextTurn(): Promise<Unit> = Promise { resolve, _ -> js("setImmediate")({ resolve(Unit) }) }
internal fun utf8Bytes(value: String): Int = js("Buffer.byteLength(value)")
internal fun globalBrowser(): dynamic = js("globalThis.browser")
internal fun setGlobalBrowser(value: dynamic) {
    js("globalThis.browser = value")
}
