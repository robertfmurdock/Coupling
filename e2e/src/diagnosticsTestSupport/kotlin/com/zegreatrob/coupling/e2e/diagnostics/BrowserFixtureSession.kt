package com.zegreatrob.coupling.e2e.diagnostics

import kotlinx.coroutines.await
import kotlin.js.Promise

internal external interface DriverProcess {
    var exitCode: Int?
    var signalCode: String?
    var kill: (String) -> Boolean
    fun once(event: String, listener: () -> Unit)
}

internal external interface BrowserSession {
    fun deleteSession(): Promise<Unit>
    fun url(url: String): Promise<Unit>
    fun execute(script: String): Promise<dynamic>
    fun waitUntil(condition: () -> Promise<Boolean>, options: WaitOptions): Promise<Unit>
    fun performActions(actions: Array<PointerSequence>): Promise<Unit>
    fun releaseActions(): Promise<Unit>
}
internal external interface WebdriverModule {
    fun remote(options: BrowserOptions): Promise<BrowserSession>
}
internal external interface DriverModule {
    fun startWebDriver(options: BrowserOptions): Promise<DriverProcess>
}

// Own the process before requesting a session, including when Chrome crashes
// before remote() can return an object on which deleteSession() is available.
internal class BrowserFixtureSession(private val driver: DriverProcess) {
    private var stopped = false
    private var browser: BrowserSession? = null

    suspend fun open(connect: suspend () -> BrowserSession): BrowserSession = try {
        connect().also { browser = it }
    } catch (primary: Throwable) {
        runCatching { stopDriver() }.onFailure(primary::addSuppressed)
        throw primary
    }

    suspend fun close() {
        try {
            browser?.deleteSession()?.await()
        } finally {
            stopDriver()
        }
    }

    private suspend fun stopDriver() {
        if (stopped) return
        stopped = true
        if (driver.exitCode != null || driver.signalCode != null) return
        val exited = Promise<Unit> { resolve, _ -> driver.once("exit") { resolve(Unit) } }
        driver.kill("SIGKILL")
        exited.await()
    }
}
