package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.sdk.js.Process
import mu.KotlinLoggingConfiguration

actual fun getEnv(name: String): String? = Process.getEnv(name)

actual fun setupPlatformSpecificKtorSettings() {
    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
    js("process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = '0'")
}
