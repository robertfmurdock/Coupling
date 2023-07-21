package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.logging.JsonFormatter
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

fun main() {
    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
    js("process.env.DYNAMO_PREFIX = 'test-'")
}
