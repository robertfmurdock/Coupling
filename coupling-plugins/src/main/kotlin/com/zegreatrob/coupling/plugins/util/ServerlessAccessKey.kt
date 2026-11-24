package com.zegreatrob.coupling.plugins.util

import com.zegreatrob.coupling.plugins.js.NodeExec
import org.gradle.api.provider.Provider

fun NodeExec.useServerlessAccessKey(lookup: (String) -> Provider<String> = project.providers::fetchAwsSsmParameter) {
    val key = lookup("/local/SERVERLESS_ACCESS_KEY")
    doFirst {
        (this as NodeExec).environment("SERVERLESS_ACCESS_KEY", key.get())
    }
}
