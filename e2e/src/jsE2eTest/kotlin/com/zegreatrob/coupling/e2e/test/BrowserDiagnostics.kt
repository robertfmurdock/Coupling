package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.testlogging.withTestDiagnostics
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

@JsName("browser")
private external object DiagnosticBrowser {
    val couplingDiagnostics: BrowserDiagnosticCollector
}

private external interface BrowserDiagnosticCollector {
    fun drainLogs(): Promise<Array<Json>>
    fun markBootstrapFailure(failure: Throwable)
}

suspend fun browserLogs(): List<Json> = DiagnosticBrowser.couplingDiagnostics.drainLogs().await().toList()

suspend fun <T> withBootstrapDiagnostics(action: suspend () -> T): T = withTestDiagnostics({ browserLogs().forwardLogs() }) {
    try {
        action()
    } catch (failure: Throwable) {
        runCatching { DiagnosticBrowser.couplingDiagnostics.markBootstrapFailure(failure) }
            .exceptionOrNull()
            ?.let(failure::addSuppressed)
        throw failure
    }
}
