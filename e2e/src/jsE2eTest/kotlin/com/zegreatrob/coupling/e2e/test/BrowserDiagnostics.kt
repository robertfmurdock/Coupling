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

suspend fun <T> withBootstrapDiagnostics(
    diagnostics: suspend () -> Unit = { checkLogs() },
    markFailure: (Throwable) -> Unit = { DiagnosticBrowser.couplingDiagnostics.markBootstrapFailure(it) },
    action: suspend () -> T,
): T = try {
    withTestDiagnostics({ diagnostics() }) { action() }
} catch (failure: Throwable) {
    runCatching { markFailure(failure) }
        .exceptionOrNull()
        ?.let(failure::addSuppressed)
    throw failure
}
