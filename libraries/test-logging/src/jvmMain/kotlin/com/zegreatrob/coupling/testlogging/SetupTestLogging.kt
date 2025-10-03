package com.zegreatrob.coupling.testlogging

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class SetupTestLogging : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) = JsonLoggingTestMintsReporter.initialize()
}
