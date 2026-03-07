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

tasks.withType(AbstractTestTask::class).configureEach {
    dependsOn(writeLogConfig)
    val jsonLoggingListener = JsonLoggingTestListener(path, testRunIdentifier)
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
    val existingNodeOptions = System.getenv("NODE_OPTIONS")
    val nodeOptions = listOf(existingNodeOptions, "--require $hookPath")
        .filterNotNull()
        .joinToString(" ")
        .trim()
    environment("NODE_OPTIONS", nodeOptions)
    doFirst {
        TestLoggingFileAppender.appendTestmintsLog(logFilePath, taskPath, "js-test-start")
    }
    doLast {
        TestLoggingFileAppender.appendTestmintsLog(logFilePath, taskPath, "js-test-finish")
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
