package com.zegreatrob.coupling.e2e.diagnostics

import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
internal sealed external interface CollectorOptions {
    val directory: String?
    val identity: (() -> TestIdentity)?
    val limits: EvidenceLimits?
    val storage: Storage?
}

@JsPlainObject
internal sealed external interface TestIdentity {
    val suite: String
    val testId: String
}

@JsPlainObject
internal sealed external interface EvidenceLimits {
    val entries: Int
    val bytes: Int
    val entryBytes: Int
}

@JsPlainObject
internal sealed external interface Storage {
    val mkdir: ((String, dynamic) -> Promise<Unit>)?
    val writeFile: ((String, String, dynamic) -> Promise<Unit>)?
}

@JsPlainObject
internal sealed external interface BrowserOptions {
    val logLevel: String
    val connectionRetryCount: Int
    val connectionRetryTimeout: Int
    val capabilities: BrowserCapabilities
}

@JsPlainObject
internal sealed external interface BrowserCapabilities {
    val browserName: String
    val browserVersion: String
    val acceptInsecureCerts: Boolean

    @JsName("goog:chromeOptions")
    val chromeOptions: ChromeOptions
}

@JsPlainObject
internal sealed external interface ChromeOptions {
    val args: Array<String>
    val binary: String?
}

@JsPlainObject
internal sealed external interface WaitOptions {
    val timeout: Int
}

@JsPlainObject
internal sealed external interface ConsoleEvent {
    val type: String
    val level: String
    val text: String
    val timestamp: Int
}

@JsPlainObject
internal sealed external interface PointerSequence {
    val type: String
    val id: String
    val parameters: PointerParameters
    val actions: Array<PointerAction>
}

@JsPlainObject
internal sealed external interface PointerParameters {
    val pointerType: String
}

@JsPlainObject
internal sealed external interface PointerAction {
    val type: String
    val duration: Int?
    val x: Int?
    val y: Int?
    val button: Int?
}
