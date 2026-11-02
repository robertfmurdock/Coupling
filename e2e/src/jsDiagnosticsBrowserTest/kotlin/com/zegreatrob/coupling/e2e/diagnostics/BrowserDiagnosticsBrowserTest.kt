package com.zegreatrob.coupling.e2e.diagnostics

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test

class BrowserDiagnosticsBrowserTest {
    @Test
    fun realChromePreservesPreBootstrapConsoleScriptAndNetworkEvidence() = asyncSetup(BrowserFixture()) {
        open()
        waitFor("return window.fixtureFinished === true")
    } exercise {
        val primary = nativeError("deliberate bootstrap failure")
        collector.markBootstrapFailure(primary)
        val filename = collector.captureFailure(primary).await()
        val duplicate = collector.captureFailure(nativeError("cached bootstrap failure")).await()
        Triple(filename, duplicate, report(filename))
    } verifyAnd { (filename, duplicate, report) ->
        duplicate.assertIsEqualTo(filename)
        report.events().any { it.text("message")?.contains("fixture warning") == true && it.text("level") == "WARN" }.assertIsEqualTo(true)
        report.events().any { it.text("message")?.contains("fixture console error") == true && it.text("level") == "ERROR" }.assertIsEqualTo(true)
        report.events().any { it.text("kind") == "javascript" && it.text("message")?.contains("fixture uncaught exception") == true }.assertIsEqualTo(true)
        report.events().any { it.text("source") == "network" && it.text("status") == "503" }.assertIsEqualTo(true)
        report.events().any { it.text("source") == "network" && it.text("error") != null }.assertIsEqualTo(true)
        report.getValue("snapshot").jsonObject.number("mountChildren").assertIsEqualTo(0)
        (Files.stat(Path.join(directory, report.text("screenshot")!!)).await().size > 0).assertIsEqualTo(true)
        Files.readFile(filename, "utf8").await().contains("fixture-secret").assertIsEqualTo(false)
        report.errors().assertIsEqualTo(emptyList())
        println("Browser diagnostic fixture artifact: $filename")
    } teardown { close() }

    @Test
    fun maintainedPolyfillDeliversTouchDragEnterAndDropWithoutWarnings() = asyncSetup(BrowserFixture(touch = true)) {
        open()
    } exercise { drag() } verifyAnd {
        (browser.execute("return window.entered === true").await() as Boolean).assertIsEqualTo(true)
        collector.drainLogs().await().map(::jsonTree).filter { it.text("level") in listOf("WARN", "ERROR") }.assertIsEqualTo(emptyList())
    } teardown { close() }
}
