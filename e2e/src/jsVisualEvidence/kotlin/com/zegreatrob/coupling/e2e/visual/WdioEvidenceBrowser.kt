package com.zegreatrob.coupling.e2e.visual

import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

// Binding gaps in jsmints' Browser interface are kept at this adapter boundary.
@JsName("browser")
external val CaptureWdio: EvidenceWdio

external interface EvidenceWdio {
    val capabilities: CaptureCapabilities
    fun setViewport(options: ViewportOptions): Promise<Unit>
    fun emulate(scope: String, value: String): Promise<Unit>
    fun execute(script: String): Promise<dynamic>
    fun execute(script: String, argument: String): Promise<dynamic>
    fun takeScreenshot(): Promise<String>
}

external interface CaptureCapabilities {
    val browserName: String
    val browserVersion: String
}

@JsPlainObject
external interface ViewportOptions {
    val width: Int
    val height: Int
    val devicePixelRatio: Int
}

@JsModule("node:buffer")
private external object CaptureBufferModule {
    val Buffer: CaptureBufferFactory
}
private external interface CaptureBufferFactory {
    fun from(value: String, encoding: String): CaptureBuffer
}
private external interface CaptureBuffer {
    val length: Int
    fun readUInt32BE(offset: Int): Int
    fun toString(encoding: String, start: Int, end: Int): String
}

class WdioEvidenceBrowser(private val browser: EvidenceWdio = CaptureWdio) : EvidenceBrowser {
    override val identity get() = BrowserIdentity(browser.capabilities.browserName, browser.capabilities.browserVersion)

    override suspend fun setViewport(dimensions: Dimensions) {
        browser.emulate("colorScheme", "light").await()
        browser.emulate("locale", "en-US").await()
        browser.emulate("timezone", "UTC").await()
        browser.setViewport(ViewportOptions(dimensions.width, dimensions.height, devicePixelRatio = 1)).await()
    }

    override suspend fun measure(): ViewportMeasurement {
        val result = browser.execute(
            """
            return {width: innerWidth, height: innerHeight, deviceScale: devicePixelRatio,
                theme: matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'};
            """.trimIndent(),
        ).await()
        return ViewportMeasurement(Dimensions(result.width as Int, result.height as Int), result.deviceScale as Double, result.theme as String)
    }

    override suspend fun screenshot(): CapturedImage {
        val base64 = browser.takeScreenshot().await()
        val image = CaptureBufferModule.Buffer.from(base64, "base64")
        check(
            image.length >= 33 && image.toString("hex", 0, 8) == "89504e470d0a1a0a" &&
                image.toString("ascii", 12, 16) == "IHDR",
        ) { "Invalid PNG screenshot" }
        return CapturedImage(base64, Dimensions(image.readUInt32BE(16), image.readUInt32BE(20)))
    }
}
