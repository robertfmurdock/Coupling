package com.zegreatrob.coupling.plugins

internal object TestAttributionHelper {
    fun inferPlatform(task: String): String = when {
        task.contains("jvm", ignoreCase = true) -> "jvm"
        task.contains("e2e", ignoreCase = true) -> "e2e"
        else -> "js"
    }

    fun addAttribution(
        properties: Map<String, Any?>,
        logger: String,
        testIdentity: TestIdentityTracker.TestIdentity?,
        taskName: String,
    ): Map<String, Any?> {
        if (testIdentity == null) {
            return properties
        }
        val enriched = properties.toMutableMap().apply {
            put("test_suite", testIdentity.suite)
            put("test_name", testIdentity.test)
            put("test_id", testIdentity.testId)
            if (logger == "command") {
                put("test_task", taskName)
                put("test_platform", inferPlatform(taskName))
            }
        }
        return enriched
    }
}
