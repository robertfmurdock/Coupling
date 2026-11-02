package com.zegreatrob.coupling.e2e.diagnostics

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test

class BrowserDiagnosticsTest {
    @Test
    fun capturesPreBootstrapErrorsWithIdentityAndMetadata() = asyncSetup(DiagnosticFixture()) {
        open()
        collector.stop().await()
        collector = module.createCollector(browser, CollectorOptions(identity = { TestIdentity("bootstrap", "first") }))
        collector.start().await()
    } exercise {
        browser.emit("log.entryAdded", nativeJson("""{"type":"console","level":"error","text":"startup failed","timestamp":1234,"stackTrace":{"callFrames":[{"url":"https://example.test/app.js?token=secret","lineNumber":2,"columnNumber":3,"functionName":"start"}]}}"""))
        drain()
    } verifyAnd { entries ->
        entries.size.assertIsEqualTo(1)
        entries.single().text("level").assertIsEqualTo("ERROR")
        entries.single().number("timestamp").assertIsEqualTo(1234)
        entries.single().getValue("identity").jsonObject.text("testId").assertIsEqualTo("first")
        entries.single().getValue("stack").jsonArray.single().jsonObject.text("url").assertIsEqualTo("https://example.test/app.js")
        calls.count { it == "logs" }.assertIsEqualTo(0)
    } teardown { close() }

    @Test
    fun classicAdapterDrainsOnceAndPreservesSeverity() = asyncSetup(DiagnosticFixture()) {
        open()
        collector.stop().await()
        calls.clear()
        browser.isBidi = false
        logs.add(nativeJson("""{"level":"SEVERE","timestamp":42,"message":"page error"}"""))
        collector = module.createCollector(browser)
        collector.start().await()
    } exercise { drain() to drain() } verifyAnd { (first, second) ->
        first.single().text("level").assertIsEqualTo("ERROR")
        first.single().number("timestamp").assertIsEqualTo(42)
        second.assertIsEqualTo(emptyList())
        calls.assertIsEqualTo(listOf("logs", "logs"))
    } teardown { close() }

    @Test
    fun evictionCannotHideFailuresAndEvidenceStaysBounded() = asyncSetup(DiagnosticFixture(CollectorOptions(limits = EvidenceLimits(2, 800, 250)))) {
        open()
    } exercise {
        browser.emit("log.entryAdded", event("first error"))
        repeat(8) { browser.emit("log.entryAdded", event("x".repeat(1000), "info")) }
        capture() to drain()
    } verifyAnd { (report, logs) ->
        (report.events().size <= 2).assertIsEqualTo(true)
        (utf8Bytes(report.getValue("events").toString()) <= 802).assertIsEqualTo(true)
        (report.number("dropped") > 0).assertIsEqualTo(true)
        (report.number("truncated") > 0).assertIsEqualTo(true)
        report.getValue("severity").jsonObject.number("ERROR").assertIsEqualTo(1)
        logs.any { it.text("level") == "ERROR" }.assertIsEqualTo(true)
    } teardown { close() }

    @Test
    fun networkMetadataExcludesSuccessesSecretsAndBodies() = asyncSetup(DiagnosticFixture()) { open() } exercise {
        val request = nativeJson("""{"url":"https://user:pass@example.test/script.js?token=secret#hidden","method":"GET","headers":[{"name":"Authorization","value":"secret"}],"body":"secret"}""")
        listOf(200, 503).forEach { status ->
            val response = nativeJson("""{"request":{},"response":{"status":$status,"headers":["secret"],"body":"secret"}}""")
            response.request = request
            browser.emit("network.responseCompleted", response)
        }
        val failure = nativeJson("""{"errorText":"connection reset"}""")
        failure.request = request
        browser.emit("network.fetchError", failure)
        capture()
    } verifyAnd { report ->
        val network = report.events().filter { it.text("source") == "network" }
        network.size.assertIsEqualTo(2)
        network.all { it.text("url") == "https://example.test/script.js" }.assertIsEqualTo(true)
        report.getValue("snapshot").jsonObject.text("url").assertIsEqualTo("https://example.test/page")
        report.toString().contains("secret").assertIsEqualTo(false)
        report.getValue("severity").toString().assertIsEqualTo("""{"WARN":0,"ERROR":0}""")
    } teardown { close() }

    @Test
    fun successAddsNoRoundTripsAndFailureCapturesOnce() = asyncSetup(DiagnosticFixture()) {
        open()
    } exercise {
        val success = drain() to calls.toList()
        val failure = nativeError("bootstrap failed")
        Triple(success, collector.captureFailure(failure).await(), collector.captureFailure(failure).await())
    } verifyAnd { (success, first, second) ->
        success.first.assertIsEqualTo(emptyList())
        success.second.assertIsEqualTo(listOf("subscribe"))
        second.assertIsEqualTo(first)
        calls.count { it == "snapshot" }.assertIsEqualTo(1)
        calls.count { it == "screenshot" }.assertIsEqualTo(1)
        Files.readdir(directory).await().count { it.endsWith(".json") }.assertIsEqualTo(1)
    } teardown { close() }

    @Test
    fun deadlineRetainsInitialEvidenceAndSkipsScreenshot() = asyncSetup(DiagnosticFixture()) {
        open()
        fakeTime()
        browser.emit("log.entryAdded", event("before disconnect"))
    } exercise {
        val entered = Gate()
        browser.execute = {
            promise {
                entered.release()
                Gate().waitFor()
            }
        }
        val capture = collector.captureFailure(nativeError("timeout"))
        entered.waitFor()
        expire()
        report(capture.await())
    } verifyAnd { report ->
        report.events().single().text("message").assertIsEqualTo("before disconnect")
        report.errors().any { it.contains("deadline") }.assertIsEqualTo(true)
        calls.count { it == "screenshot" }.assertIsEqualTo(0)
    } teardown { close() }

    @Test
    fun stopDisposesListenersAndSubscription() = asyncSetup(DiagnosticFixture()) { open() } exercise {
        collector.stop().await()
    } verifyAnd {
        browser.listenerCount("log.entryAdded").assertIsEqualTo(0)
        calls.count { it == "unsubscribe" }.assertIsEqualTo(1)
    } teardown { close() }

    @Test
    fun sanitizesClassicAndBidiStructuredPayloads() = asyncSetup(DiagnosticFixture()) { open() } exercise {
        val payload = """{"level":"ERROR","access_token":"token-value","headers":{"Authorization":"credential-value"},"body":"body-value","message":"https://user:pass@example.test/x?secret=url-value#private"}"""
        val chrome = nativeJson("""{"level":"INFO"}""")
        chrome.message = "https://example.test/app.js 1:1 ${JSON.stringify(payload)}"
        browser.emit("log.entryAdded", chrome)
        browser.emit("log.entryAdded", event(payload, "info"))
        drain()
    } verifyAnd { entries ->
        entries.size.assertIsEqualTo(2)
        entries.forEach { entry ->
            entry.text("level").assertIsEqualTo("ERROR")
            Regex("token-value|credential-value|body-value|url-value").containsMatchIn(entry.toString()).assertIsEqualTo(false)
        }
    } teardown { close() }

    @Test
    fun totalDeadlineIncludesStalledStorage() = asyncSetup(DiagnosticFixture()) {
        open()
        fakeTime()
    } exercise {
        val entered = Gate()
        val options = CollectorOptions(
            storage = Storage(
                mkdir = { _, _ ->
                    promise {
                        entered.release()
                        Gate().waitFor()
                    }
                },
                writeFile = { _, _, _ -> error("must not write") },
            ),
        )
        collector.stop().await()
        collector = module.createCollector(browser, options)
        collector.start().await()
        val capture = collector.captureFailure(nativeError("failure"))
        entered.waitFor()
        expire()
        runCatching { capture.await() }
    } verifyAnd { result ->
        result.exceptionOrNull()?.message?.contains("deadline").assertIsEqualTo(true)
    } teardown { close() }

    @Test
    fun lateScreenshotCannotAttachToNextTest() = asyncSetup(DiagnosticFixture()) {
        open()
        fakeTime()
    } exercise {
        val entered = Gate()
        val finish = Gate()
        browser.saveScreenshot = {
            promise {
                entered.release()
                finish.waitFor()
            }
        }
        val attached = mutableListOf<String>()
        val listener: (String) -> Unit = { attached.add(it) }
        nodeProcess.on("test:screenshot", listener)
        after { nodeProcess.off("test:screenshot", listener) }
        val capture = collector.captureFailure(nativeError("failure"))
        entered.waitFor()
        expire()
        capture.await()
        finish.release()
        nextTurn().await()
        attached
    } verifyAnd { attached -> attached.assertIsEqualTo(emptyList()) } teardown { close() }

    @Test
    fun timedOutFinalWritePreservesInitialBundle() = asyncSetup(DiagnosticFixture()) {
        open()
        fakeTime()
    } exercise {
        val entered = Gate()
        val finish = Gate()
        var writes = 0
        val options = CollectorOptions(
            directory = directory,
            storage = Storage(writeFile = { filename, data, settings ->
                promise {
                    if (++writes == 2) {
                        entered.release()
                        finish.waitFor()
                    }
                    Files.writeFile(filename, data, settings).await()
                }
            }),
        )
        collector.stop().await()
        collector = module.createCollector(browser, options)
        collector.start().await()
        val capture = collector.captureFailure(nativeError("failure"))
        entered.waitFor()
        expire()
        val filename = capture.await()
        val before = report(filename)
        finish.release()
        nextTurn().await()
        before to report(filename)
    } verifyAnd { (before, after) ->
        before.text("captureComplete").assertIsEqualTo("false")
        before.errors().any { it.contains("deadline") }.assertIsEqualTo(true)
        after.assertIsEqualTo(before)
    } teardown { close() }

    @Test
    fun runnerInstallsBeforeBootstrapAndDeduplicatesSharedFailure() = asyncSetup(DiagnosticFixture()) {
        open()
        collector.stop().await()
        calls.clear()
        val previous = globalBrowser()
        setGlobalBrowser(browser)
        after { setGlobalBrowser(previous) }
    } exercise {
        var previousAfterCalls = 0
        val options = nativeJson("{}")
        options.before = {
            browser.listenerCount("log.entryAdded").assertIsEqualTo(1)
            browser.emit("log.entryAdded", event("early startup failure"))
        }
        options.afterTest = { previousAfterCalls++ }
        val directories = nativeJson("{}")
        directories.reports = directory
        val config = module.configure(options, directories)
        after { config.after().await() }
        config.before().await()
        config.beforeTest(nativeJson("""{"title":"first test","parent":"fixture","uid":"test-one"}""")).await()
        browser.couplingDiagnostics.markBootstrapFailure(nativeError("bootstrap"))
        repeat(2) {
            val result = nativeJson("""{"passed":false}""")
            result.error = nativeError("bootstrap")
            config.afterTest(nativeJson("""{"title":"test $it"}"""), nativeJson("{}"), result).await()
        }
        config.after().await()
        previousAfterCalls
    } verifyAnd { previousAfterCalls ->
        calls.count { it == "screenshot" }.assertIsEqualTo(1)
        previousAfterCalls.assertIsEqualTo(2)
        browser.listenerCount("log.entryAdded").assertIsEqualTo(0)
    } teardown { close() }

    @Test
    fun disconnectedBidiSessionNeverFallsBackToClassic() = asyncSetup(DiagnosticFixture()) { open() } exercise {
        browser.emit("log.entryAdded", event("before connection loss"))
        browser.isBidi = false
        browser.getLogs = { error("classic polling after disconnect") }
        val entries = drain()
        collector.stop().await()
        entries
    } verifyAnd { entries ->
        entries.single().text("message").assertIsEqualTo("before connection loss")
        calls.count { it == "unsubscribe" }.assertIsEqualTo(0)
    } teardown { close() }
}

class BrowserFixtureSessionTest {
    @Test
    fun failedStartupStopsOwnedDriverAndPreservesOriginalError() = asyncSetup(SessionContext()) {
        val emitter = EventEmitter()
        driver = emitter.unsafeCast<DriverProcess>()
        driver.exitCode = null
        driver.signalCode = null
        driver.kill = { signal ->
            calls.add(signal)
            driver.signalCode = signal
            emitter.emit("exit")
            true
        }
        session = BrowserFixtureSession(driver)
    } exercise {
        runCatching { session.open { throw primary } }
    } verifyAnd { result ->
        result.exceptionOrNull().assertIsEqualTo(primary)
        calls.assertIsEqualTo(listOf("SIGKILL"))
    } teardown { session.close() }

    @Test
    fun successfulSessionDeletesAndStopsOwnedDriverOnce() = asyncSetup(SessionContext()) {
        val emitter = EventEmitter()
        driver = emitter.unsafeCast<DriverProcess>()
        driver.exitCode = null
        driver.signalCode = null
        driver.kill = { signal ->
            calls.add(signal)
            driver.signalCode = signal
            emitter.emit("exit")
            true
        }
        session = BrowserFixtureSession(driver)
        val browser = nativeJson("{}")
        browser.deleteSession = {
            calls.add("deleteSession")
            kotlin.js.Promise.resolve(Unit)
        }
        session.open { browser.unsafeCast<BrowserSession>() }
    } exercise { session.close() } verify {
        calls.assertIsEqualTo(listOf("deleteSession", "SIGKILL"))
    }
}

private class SessionContext {
    val primary = IllegalStateException("tab crashed")
    val calls = mutableListOf<String>()
    lateinit var driver: DriverProcess
    lateinit var session: BrowserFixtureSession
}
