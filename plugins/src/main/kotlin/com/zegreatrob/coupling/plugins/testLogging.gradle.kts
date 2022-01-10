package com.zegreatrob.coupling.plugins

import gradle.kotlin.dsl.accessors._0def0b2a311a48a48a92e7be672fd977.ext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder
import org.apache.tools.ant.util.TeeOutputStream
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import java.io.OutputStream
import java.util.*

val nodeLogger = LogManager.getLogger("node-exec")

afterEvaluate {
    val testRunIdentifier: String = getTestRunIdentifier()

    tasks.withType(KotlinJsTest::class) {
        val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier)
        addTestListener(jsonLoggingListener)
        addTestOutputListener(jsonLoggingListener)
    }

    tasks.withType(NodeJsExec::class) {
        val buildOutputStream = IoBuilder.forLogger(nodeLogger)
            .buildOutputStream()

        val existingOutput: OutputStream? = standardOutput

        standardOutput = if (existingOutput != null)
            TeeOutputStream(
                existingOutput,
                buildOutputStream
            ) else {
            buildOutputStream
        }
    }
}

fun Project.getTestRunIdentifier(): String {
    var testRunIdentifier: Any? by rootProject.ext
    return if (testRunIdentifier != null)
        "$testRunIdentifier"
    else {
        UUID.randomUUID().toString().also {
            testRunIdentifier = it
        }
    }
}
