package com.zegreatrob.coupling.e2e.diagnostics

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Promise

internal fun nativeJson(text: String): dynamic = JSON.parse<dynamic>(text)
internal fun jsonTree(value: dynamic): JsonObject = Json.parseToJsonElement(JSON.stringify(value)).jsonObject
internal fun JsonObject.text(key: String): String? = get(key)?.jsonPrimitive?.content
internal fun JsonObject.number(key: String): Int = text(key)!!.toInt()
internal fun JsonObject.events(): List<JsonObject> = getValue("events").jsonArray.map { it.jsonObject }
internal fun JsonObject.errors(): List<String> = getValue("captureErrors").jsonArray.map { it.jsonPrimitive.content }
internal fun event(text: String, level: String = "error"): ConsoleEvent = ConsoleEvent("console", level, text, 1234)
internal fun <T> promise(block: suspend () -> T): Promise<T> = CoroutineScope(Dispatchers.Default).promise { block() }

internal class DiagnosticFixture(private val options: CollectorOptions = CollectorOptions()) {
    lateinit var module: DiagnosticsModule
    lateinit var browser: BrowserDouble
    lateinit var collector: Collector
    lateinit var directory: String
    val calls = mutableListOf<String>()
    val logs = mutableListOf<dynamic>()
    private val cleanup = mutableListOf<suspend () -> Unit>()

    suspend fun open() {
        module = importModule<DiagnosticsModule>(nodeProcess.env.diagnosticsModule).await()
        directory = Files.mkdtemp(Path.join(Os.tmpdir(), "coupling-browser-diagnostics-")).await()
        after { Files.rm(directory, nativeJson("""{"recursive":true,"force":true}""")).await() }
        browser = EventEmitter().unsafeCast<BrowserDouble>().apply {
            isBidi = true
            sessionId = "fixture-session"
            sessionSubscribe = {
                calls.add("subscribe")
                Promise.resolve(Unit)
            }
            sessionUnsubscribe = {
                calls.add("unsubscribe")
                Promise.resolve(Unit)
            }
            getLogs = {
                calls.add("logs")
                val result = logs.toTypedArray()
                logs.clear()
                Promise.resolve(result)
            }
            execute = {
                calls.add("snapshot")
                Promise.resolve(nativeJson("""{"url":"https://user:password@example.test/page?token=secret#private","readyState":"complete","mountChildren":0}"""))
            }
            saveScreenshot = {
                calls.add("screenshot")
                Promise.resolve(Unit)
            }
        }
        collector = module.createCollector(
            browser,
            CollectorOptions(
                directory = directory,
                identity = options.identity,
                limits = options.limits,
                storage = options.storage,
            ),
        )
        after { collector.stop().await() }
        collector.start().await()
    }

    fun after(action: suspend () -> Unit) {
        cleanup.add(action)
    }
    fun fakeTime() {
        NodeTest.mock.timers.enable(nativeJson("""{"apis":["setTimeout"]}"""))
        after { NodeTest.mock.timers.reset() }
    }
    fun expire() = NodeTest.mock.timers.tick(5000)
    suspend fun report(filename: String): JsonObject = Json.parseToJsonElement(Files.readFile(filename, "utf8").await()).jsonObject
    suspend fun capture(): JsonObject = report(collector.captureFailure(nativeError("startup failed")).await())
    suspend fun drain(): List<JsonObject> = collector.drainLogs().await().map(::jsonTree)
    suspend fun close() {
        var failure: Throwable? = null
        cleanup.reversed().forEach { action ->
            runCatching { action() }.onFailure { error -> if (failure == null) failure = error else failure!!.addSuppressed(error) }
        }
        failure?.let { throw it }
    }
}

internal class Gate {
    private val deferred = CompletableDeferred<Unit>()
    fun release() = deferred.complete(Unit)
    suspend fun waitFor() = deferred.await()
}
