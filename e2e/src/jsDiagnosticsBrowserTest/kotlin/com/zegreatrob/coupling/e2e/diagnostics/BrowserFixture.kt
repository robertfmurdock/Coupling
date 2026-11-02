package com.zegreatrob.coupling.e2e.diagnostics

import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.js.Promise
import kotlin.uuid.Uuid

@JsModule("node:http")
private external object Http {
    fun createServer(handler: (Request, Response) -> Unit): Server
}
private external interface Request {
    val url: String
    val socket: Socket
}
private external interface Socket {
    fun destroy()
}
private external interface Response {
    fun writeHead(status: Int)
    fun setHeader(name: String, value: String)
    fun end(body: String = definedExternally)
}
private external interface Address {
    val port: Int
}
private external interface Server {
    fun listen(port: Int, hostname: String, ready: () -> Unit)
    fun address(): Address
    fun closeAllConnections()
    fun close(closed: () -> Unit)
}

internal class BrowserFixture(private val touch: Boolean = false) {
    lateinit var browser: BrowserSession
    lateinit var collector: Collector
    private var session: BrowserFixtureSession? = null
    private var server: Server? = null
    private var pointerActive = false
    lateinit var directory: String

    suspend fun open() {
        try {
            start()
        } catch (primary: Throwable) {
            runCatching { close() }.onFailure(primary::addSuppressed)
            throw primary
        }
    }

    private suspend fun start() {
        val root = nodeProcess.env.repositoryRoot
        val polyfill = if (touch) Files.readFile(Path.join(root, "build/js/node_modules/mobile-drag-drop/index.min.js"), "utf8").await() else ""
        val activeServer = Http.createServer { request, response ->
            when (request.url.substringBefore('?')) {
                "/reset" -> request.socket.destroy()

                "/error" -> {
                    response.writeHead(503)
                    response.end("unavailable")
                }

                "/favicon.ico" -> {
                    response.writeHead(204)
                    response.end()
                }

                "/polyfill.js" -> {
                    response.setHeader("Content-Type", "application/javascript")
                    response.end(polyfill)
                }

                else -> {
                    response.setHeader("Content-Type", "text/html")
                    response.end(if (touch) touchPage else diagnosticsPage)
                }
            }
        }
        server = activeServer
        Promise<Unit> { resolve, _ -> activeServer.listen(0, "127.0.0.1") { resolve(Unit) } }.await()
        val binary = nodeProcess.env.chromeBinary
        val chromeOptions = if (binary.isNullOrBlank()) {
            ChromeOptions(args = arrayOf("--headless"))
        } else {
            ChromeOptions(args = arrayOf("--headless"), binary = binary)
        }
        val options = BrowserOptions(
            logLevel = "error",
            connectionRetryCount = 0,
            connectionRetryTimeout = 10000,
            capabilities = BrowserCapabilities(
                browserName = "chrome",
                browserVersion = "stable",
                acceptInsecureCerts = true,
                chromeOptions = chromeOptions,
            ),
        )
        val driverModule = importModule<DriverModule>(Path.join(root, "build/js/node_modules/@wdio/utils/build/node.js")).await()
        val webdriver = importModule<WebdriverModule>(Path.join(root, "build/js/node_modules/webdriverio/build/node.js")).await()
        val ownedSession = BrowserFixtureSession(driverModule.startWebDriver(options).await())
        session = ownedSession
        browser = ownedSession.open { webdriver.remote(options).await() }
        val module = importModule<DiagnosticsModule>(nodeProcess.env.diagnosticsModule).await()
        directory = Path.join(root, "e2e/build/reports/browser-diagnostics-fixture", Uuid.random().toString())
        collector = module.createCollector(
            browser.unsafeCast<BrowserDouble>(),
            CollectorOptions(
                directory = directory,
                identity = { TestIdentity("diagnostic-fixture", "bootstrap") },
            ),
        )
        collector.start().await()
        browser.url("http://127.0.0.1:${activeServer.address().port}/").await()
    }

    suspend fun waitFor(script: String, timeout: Int = 5000) {
        browser.waitUntil({ promise { browser.execute(script).await() as Boolean } }, WaitOptions(timeout)).await()
    }

    suspend fun report(filename: String): JsonObject = Json.parseToJsonElement(Files.readFile(filename, "utf8").await()).jsonObject

    suspend fun drag() {
        pointerActive = true
        browser.performActions(
            arrayOf(
                PointerSequence(
                    type = "pointer",
                    id = "touch",
                    parameters = PointerParameters("touch"),
                    actions = arrayOf(
                        PointerAction("pointerMove", duration = 0, x = 90, y = 90),
                        PointerAction("pointerDown", button = 0),
                        PointerAction("pointerMove", duration = 200, x = 110, y = 90),
                        PointerAction("pause", duration = 200),
                        PointerAction("pointerMove", duration = 300, x = 290, y = 90),
                        PointerAction("pause", duration = 200),
                        PointerAction("pointerUp", button = 0),
                    ),
                ),
            ),
        ).await()
        browser.releaseActions().await()
        pointerActive = false
        waitFor("return window.dropped === 'payload'", 2000)
    }

    suspend fun close() {
        var failure: Throwable? = null
        suspend fun clean(action: suspend () -> Unit) {
            runCatching { action() }.onFailure { if (failure == null) failure = it else failure!!.addSuppressed(it) }
        }
        if (pointerActive) clean { browser.releaseActions().await() }
        if (::collector.isInitialized) clean { collector.stop().await() }
        clean { session?.close() }
        server?.let { activeServer ->
            clean {
                activeServer.closeAllConnections()
                Promise<Unit> { resolve, _ -> activeServer.close { resolve(Unit) } }.await()
            }
        }
        failure?.let { throw it }
    }
}

private val diagnosticsPage = """
    <div class="view-container"></div><script>
    console.warn('fixture warning'); console.error('fixture console error');
    setTimeout(() => { throw new Error('fixture uncaught exception') }, 0);
    Promise.all([fetch('/reset?token=fixture-secret').catch(() => {}), fetch('/error?token=fixture-secret')])
        .then(() => { window.fixtureFinished = true });
    </script>
""".trimIndent()

private val touchPage = """
    <style>body{touch-action:none}#source,#target{position:absolute;top:50px;width:80px;height:80px;background:#ddd}#source{left:50px}#target{left:250px}</style>
    <div id="source" draggable="true">source</div><div id="target">target</div><script src="/polyfill.js"></script><script>
    MobileDragDrop.polyfill({forceApply:true});
    document.querySelector('#source').addEventListener('dragstart', e => e.dataTransfer.setData('text/plain','payload'));
    document.querySelector('#target').addEventListener('dragenter', e => { e.preventDefault(); window.entered = true });
    document.querySelector('#target').addEventListener('dragover', e => e.preventDefault());
    document.querySelector('#target').addEventListener('drop', e => { e.preventDefault(); window.dropped = e.dataTransfer.getData('text/plain') });
    </script>
""".trimIndent()
