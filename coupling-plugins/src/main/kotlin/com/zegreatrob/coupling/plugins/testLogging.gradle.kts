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

val writeLogConfig = rootProject.tasks.let { tasks ->
    tasks.findByName("writeTestLog4j2Config")?.let { tasks.named(it.name) }
        ?: tasks.register("writeTestLog4j2Config", WriteTestLog4j2Config::class.java) {
            logFilePath.set(logFilePathProvider)
            outputFile.set(logConfigFile)
            doFirst {
                val configPath = outputFile.get().asFile
                configPath.parentFile.mkdirs()
            }
        }
}

val resetTestJsonl = rootProject.tasks.let { tasks ->
    tasks.findByName("resetTestJsonl")?.let { tasks.named(it.name) }
        ?: tasks.register("resetTestJsonl") {
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
}

tasks.withType(AbstractTestTask::class).configureEach {
    dependsOn(resetTestJsonl)
    dependsOn(writeLogConfig)
    val taskPath = path
    val logFilePath = logFilePathProvider
    doFirst {
        val jsonLoggingListener = JsonLoggingTestListener(taskPath, testRunIdentifier, logFilePath.get())
        addTestListener(jsonLoggingListener)
        addTestOutputListener(jsonLoggingListener)

        val logConfigPath = logConfigFile.get().asFile
        if (logConfigPath.exists()) {
            System.setProperty("log4j2.configurationFile", logConfigPath.absolutePath)
            Configurator.initialize(null, logConfigPath.absolutePath)
        }
    }
}

tasks.withType(Test::class).configureEach {
    systemProperty("log4j2.configurationFile", logConfigFile.get().asFile.absolutePath)
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    useJUnitPlatform()
}

val slf4jTestBackend = "org.slf4j:slf4j-simple"
configurations.configureEach {
    if (name == "testRuntimeOnly" || name == "jvmTestRuntimeOnly") {
        project.dependencies.add(name, slf4jTestBackend)
    }
}

tasks.withType(KotlinJsTest::class).configureEach {
    val taskPath = path
    val logFilePath = logFilePathProvider
    doFirst {
        val runId = System.getProperty("testRunIdentifier") ?: "unknown-run"
        TestLoggingFileAppender.appendTestmintsLog(logFilePath.get(), taskPath, runId, "js-test-start")
    }
    doLast {
        val runId = System.getProperty("testRunIdentifier") ?: "unknown-run"
        TestLoggingFileAppender.appendTestmintsLog(logFilePath.get(), taskPath, runId, "js-test-finish")
    }
}

fun Project.getTestRunIdentifier(): String {
    val testRunIdentifier: Any? by rootProject.extra
    return if (testRunIdentifier != null) {
        "$testRunIdentifier"
    } else {
        UUID.randomUUID().toString()
    }
}
