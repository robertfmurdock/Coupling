package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.reports.DashboardTestSummaryTask
import org.gradle.api.tasks.testing.AbstractTestTask

plugins {
    base
}

val githubRunId = providers.environmentVariable("GITHUB_RUN_ID")
val dashboardTestSummary = tasks.register<DashboardTestSummaryTask>("dashboardTestSummary") {
    group = "reporting"
    description = "Summarizes JUnit reports for the current GitHub Actions run."
    reports.from((subprojects + project).map { module ->
        module.layout.buildDirectory.dir("test-results").map { directory ->
            directory.asFileTree.matching { include("**/*.xml") }
        }
    })
    reports.from(fileTree("coupling-plugins/build/test-results") { include("**/*.xml") })
    runId.set(githubRunId.map(String::toLong))
    runAttempt.set(providers.environmentVariable("GITHUB_RUN_ATTEMPT").map(String::toInt))
    headSha.set(providers.environmentVariable("DASHBOARD_HEAD_SHA").orElse(providers.environmentVariable("GITHUB_SHA")))
    summaryFile.set(layout.buildDirectory.file("dashboard/test-summary.json"))
    val runMetadata = runId
    onlyIf("GitHub Actions run metadata is available") { runMetadata.isPresent }
}

allprojects {
    val verificationTasks = tasks.matching { it.name == "check" || it is AbstractTestTask || it.name == "e2eRun" }
    verificationTasks.configureEach {
        finalizedBy(dashboardTestSummary)
    }
    dashboardTestSummary.configure { mustRunAfter(verificationTasks) }
}
