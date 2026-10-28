package com.zegreatrob.coupling.plugins.testlogging

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import java.io.File

data class AttributionCoverage(
    val inScope: Int,
    val fullyAttributed: Int,
    val missingAny: Int,
    val ratio: Double,
)

fun readAttributionCoverage(reportFilePath: String): AttributionCoverage {
    val reportFile = File(reportFilePath)
    if (!reportFile.exists()) {
        throw org.gradle.api.GradleException("analyze report file not found: $reportFilePath")
    }
    val report = ObjectMapper().readTree(reportFile)
    return AttributionCoverage(
        inScope = report.get("command_events_in_attribution_scope")?.asInt() ?: 0,
        fullyAttributed = report.get("command_events_with_full_test_attribution")?.asInt() ?: 0,
        missingAny = report.get("command_events_missing_any_test_attribution")?.asInt() ?: 0,
        ratio = report.get("command_events_with_full_test_attribution_ratio")?.asDouble() ?: 0.0,
    )
}

fun Project.registerTestLogCliTask(
    name: String,
    command: String,
    reportFilePath: Provider<String>,
    descriptionText: String,
    testJsonlFilePath: Provider<String>,
    testLogToolsRunner: Configuration,
    testRunIdentifier: Provider<String>? = null,
    strictFlags: List<String> = listOf("--strict"),
): TaskProvider<JavaExec> {
    val testLogToolsJavaLauncher = extensions.getByType(JavaToolchainService::class.java)
        .launcherFor { languageVersion.set(JavaLanguageVersion.of(22)) }

    return tasks.register(name, JavaExec::class.java) {
        group = "verification"
        description = descriptionText
        dependsOn(":cli:test-log-tools:jvmJar")

        classpath(testLogToolsRunner)
        javaLauncher.set(testLogToolsJavaLauncher)
        mainClass.set("com.zegreatrob.coupling.cli.testlog.MainKt")
        argumentProviders.add {
            buildList {
                addAll(
                    listOf(
                        command,
                        "--report-file",
                        reportFilePath.get(),
                        "--quiet-success",
                        "--failure-summary",
                    ),
                )
                addAll(strictFlags)
                testRunIdentifier?.orNull?.takeIf { it.isNotBlank() }?.let { add("--run-id=$it") }
                add(testJsonlFilePath.get())
            }
        }
    }
}
