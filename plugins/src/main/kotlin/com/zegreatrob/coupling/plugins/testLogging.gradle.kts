package com.zegreatrob.coupling.plugins

import gradle.kotlin.dsl.accessors._0def0b2a311a48a48a92e7be672fd977.ext
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import java.util.*

afterEvaluate {
    val testRunIdentifier: String = getTestRunIdentifier()

    tasks.withType(KotlinJsTest::class) {
        val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier)
        addTestListener(jsonLoggingListener)
        addTestOutputListener(jsonLoggingListener)
    }
}

fun Project.getTestRunIdentifier(): String {
    val testRunIdentifier: Any? = rootProject.ext.properties["testRunIdentifier"]
    return if (testRunIdentifier != null)
        "$testRunIdentifier"
    else {
        UUID.randomUUID().toString().also {
            rootProject.ext.set("testRunIdentifier", it)
        }
    }
}
