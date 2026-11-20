@file:Suppress("unused")

package com.zegreatrob.coupling.e2e.visual

import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsModule("node:fs/promises")
external object CaptureFiles {
    fun mkdir(path: String, options: DirectoryOptions): Promise<Unit>
    fun mkdtemp(prefix: String): Promise<String>
    fun readFile(path: String, encoding: String): Promise<String>
    fun writeFile(path: String, value: String, encoding: String = definedExternally): Promise<Unit>
    fun rename(from: String, to: String): Promise<Unit>
    fun rm(path: String, options: RemoveOptions): Promise<Unit>
}

@JsPlainObject
external interface DirectoryOptions {
    val recursive: Boolean
}

@JsPlainObject
external interface RemoveOptions {
    val recursive: Boolean
    val force: Boolean
}

@JsPlainObject
external interface GitOptions {
    val cwd: String
    val encoding: String
}

@JsModule("node:path")
external object CapturePath {
    fun join(vararg parts: String): String
}

@JsModule("node:crypto")
external object CaptureCrypto {
    fun randomUUID(): String
}

@JsModule("node:child_process")
external object CaptureChildProcess {
    fun execFileSync(command: String, arguments: Array<out String>, options: GitOptions): String
}

@JsModule("node:process")
private external object CaptureProcess {
    val env: kotlin.js.Json
}

fun captureEnv(name: String): String = requireNotNull(CaptureProcess.env[name] as String?) { "Missing capture setting: $name" }
