package com.zegreatrob.coupling.sdk

actual fun getEnv(name: String): String? = System.getenv(name)
actual fun setupPlatformSpecificKtorSettings() {
}
