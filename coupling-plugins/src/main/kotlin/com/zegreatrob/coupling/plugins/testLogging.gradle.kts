package com.zegreatrob.coupling.plugins

import org.apache.logging.log4j.core.config.Configurator
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import java.util.UUID

val testRunIdentifier: String = getTestRunIdentifier()

System.setProperty("testRunIdentifier", testRunIdentifier)

val logConfigFile = rootProject.layout.buildDirectory.file("test-output/log4j2-test.json")
val logFilePathProvider = rootProject.layout.buildDirectory
    .file("test-output/test.jsonl")
    .map { it.asFile.absolutePath }
val jsLogHookFile = rootProject.layout.buildDirectory.file("test-output/js-test-log-hook.js")
val logConfigPath = logConfigFile.get().asFile

logConfigPath.parentFile.mkdirs()
logConfigPath.writeText(WriteTestLog4j2Config.buildConfig(logFilePathProvider.get()))
System.setProperty("log4j2.configurationFile", logConfigPath.absolutePath)
Configurator.initialize(null, logConfigPath.absolutePath)

val writeLogConfig =
    rootProject.tasks.findByName("writeTestLog4j2Config")
        ?.let { rootProject.tasks.named(it.name) }
        ?: rootProject.tasks.register("writeTestLog4j2Config", WriteTestLog4j2Config::class.java) {
            logFilePath.set(logFilePathProvider)
            outputFile.set(logConfigFile)
        }
val writeJsLogHook =
    rootProject.tasks.findByName("writeJsTestLogHook")
        ?.let { rootProject.tasks.named(it.name) }
        ?: rootProject.tasks.register("writeJsTestLogHook", WriteJsTestLogHook::class.java) {
            logFilePath.set(logFilePathProvider)
            outputFile.set(jsLogHookFile)
        }
val resetTestJsonl =
    rootProject.tasks.findByName("resetTestJsonl")
        ?.let { rootProject.tasks.named(it.name) }
        ?: rootProject.tasks.register("resetTestJsonl") {
            val shouldReset =
                (providers.gradleProperty("coupling.testLog.reset").orNull == "true") ||
                    (System.getenv("COUPLING_TEST_LOG_RESET") == "true")
            val resetPath = logFilePathProvider.get()
            doFirst {
                if (!shouldReset) {
                    return@doFirst
                }
                val logFile = java.io.File(resetPath)
                logFile.parentFile.mkdirs()
                logFile.writeText("")
            }
        }

tasks.withType(AbstractTestTask::class).configureEach {
    dependsOn(resetTestJsonl)
    dependsOn(writeLogConfig)
    val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier, logFilePathProvider.get())
    addTestListener(jsonLoggingListener)
    addTestOutputListener(jsonLoggingListener)
}
tasks.withType(Test::class).configureEach {
    systemProperty("log4j2.configurationFile", logConfigFile.get().asFile.absolutePath)
}
tasks.withType(KotlinJsTest::class).configureEach {
    val logFilePath = logFilePathProvider.get()
    val hookPath = jsLogHookFile.get().asFile.absolutePath
    val taskPath = path
    dependsOn(writeJsLogHook)
    environment("COUPLING_TEST_LOG_PATH", logFilePath)
    environment("COUPLING_TEST_TASK_PATH", taskPath)
    environment("COUPLING_TEST_RUN_ID", testRunIdentifier)
    val existingNodeOptions = System.getenv("NODE_OPTIONS")
    val nodeOptions = listOf(existingNodeOptions, "--require $hookPath")
        .filterNotNull()
        .joinToString(" ")
        .trim()
    environment("NODE_OPTIONS", nodeOptions)
    doFirst {
        val runId = System.getProperty("testRunIdentifier") ?: "unknown-run"
        TestLoggingFileAppender.appendTestmintsLog(logFilePath, taskPath, runId, "js-test-start")
    }
    doLast {
        val runId = System.getProperty("testRunIdentifier") ?: "unknown-run"
        TestLoggingFileAppender.appendTestmintsLog(logFilePath, taskPath, runId, "js-test-finish")
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
