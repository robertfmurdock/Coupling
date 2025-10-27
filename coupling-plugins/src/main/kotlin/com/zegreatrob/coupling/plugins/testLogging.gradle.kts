package com.zegreatrob.coupling.plugins

import java.util.UUID

afterEvaluate {
    val testRunIdentifier: String = getTestRunIdentifier()

    System.setProperty("testRunIdentifier", testRunIdentifier)

    tasks.withType(AbstractTestTask::class).configureEach {
        val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier)
        addTestListener(jsonLoggingListener)
        addTestOutputListener(jsonLoggingListener)
    }
}

fun Project.getTestRunIdentifier(): String {
    val testRunIdentifier: Any? by rootProject.extra
    return if (testRunIdentifier != null)
        "$testRunIdentifier"
    else {
        UUID.randomUUID().toString()
    }
}
