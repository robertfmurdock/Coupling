package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
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
    testLogToolsClasspath: Provider<String>,
    strictFlags: List<String> = listOf("--strict"),
): TaskProvider<Exec> = tasks.register(name, Exec::class.java) {
    group = "verification"
    description = descriptionText
    dependsOn(":cli:test-log-tools:jvmJar")

    executable("java")
    argumentProviders.add {
        testLogToolsClasspath.flatMap { classpath ->
            reportFilePath.flatMap { report ->
                testJsonlFilePath.map { jsonl ->
                    listOf(
                        "-cp",
                        classpath,
                        "com.zegreatrob.coupling.cli.testlog.MainKt",
                        command,
                        "--report-file",
                        report,
                        "--quiet-success",
                        "--failure-summary",
                    ) + strictFlags + jsonl
                }
            }
        }.get()
    }
}
