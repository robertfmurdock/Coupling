package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import java.util.UUID

afterEvaluate {
    val testRunIdentifier: String = getTestRunIdentifier()

    System.setProperty("testRunIdentifier", testRunIdentifier)

    tasks.withType(KotlinJsTest::class).configureEach {
        val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier)
        addTestListener(jsonLoggingListener)
        addTestOutputListener(jsonLoggingListener)
    }
}

fun Project.getTestRunIdentifier(): String {
    var testRunIdentifier: Any? by rootProject.extra
    return if (testRunIdentifier != null)
        "$testRunIdentifier"
    else {
        UUID.randomUUID().toString().also {
            testRunIdentifier = it
        }
    }
}
