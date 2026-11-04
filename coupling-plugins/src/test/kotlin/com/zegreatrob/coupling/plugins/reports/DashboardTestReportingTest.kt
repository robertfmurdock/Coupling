package com.zegreatrob.coupling.plugins.reports

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardTestReportingTest {
    @Test
    fun `check publishes results within the same invocation`() = withFixture { directory ->
        runner(directory).build()
        assertEquals(1, readSummary(directory).get("passed").asInt())
    }

    @Test
    fun `failed check dependency still publishes failed test results`() = withFixture { directory ->
        runner(directory).withArguments("check", "-PfailTests=true", "--configuration-cache").buildAndFail()
        assertEquals(1, readSummary(directory).get("failed").asInt())
    }

    @Test
    fun `local check needs no GitHub run metadata`() = withFixture { directory ->
        val environment = System.getenv().filterKeys { !it.startsWith("GITHUB_") }
        val result = runner(directory).withEnvironment(environment).build()
        assertEquals(TaskOutcome.SKIPPED, result.task(":dashboardTestSummary")?.outcome)
    }

    private fun runner(directory: java.io.File) = GradleRunner.create()
        .withProjectDir(directory)
        .withPluginClasspath()
        .withArguments("check", "--configuration-cache")
        .withEnvironment(System.getenv() + mapOf("GITHUB_RUN_ID" to "123", "GITHUB_RUN_ATTEMPT" to "1", "GITHUB_SHA" to "abc123"))

    private fun readSummary(directory: java.io.File) = ObjectMapper().readTree(directory.resolve("build/dashboard/test-summary.json"))

    private fun withFixture(test: (java.io.File) -> Unit) {
        val directory = Files.createTempDirectory("dashboard-test-reporting").toFile()
        try {
            directory.resolve("settings.gradle.kts").writeText("rootProject.name = \"fixture\"")
            directory.resolve("build.gradle.kts").writeText(
                """
                plugins { id("com.zegreatrob.coupling.plugins.dashboardTestReporting") }
                val fixtureTest = tasks.register("e2eRun") {
                    val failTests = providers.gradleProperty("failTests").isPresent
                    val report = layout.buildDirectory.file("test-results/test/results.xml").get().asFile
                    doLast {
                        report.parentFile.mkdirs()
                        val outcome = if (failTests) "<failure/>" else ""
                        report.writeText("<testsuite><testcase name='fixture'>" + outcome + "</testcase></testsuite>")
                        if (failTests) throw GradleException("Fixture test failed")
                    }
                }
                tasks.named("check") { dependsOn(fixtureTest) }
                """.trimIndent(),
            )
            test(directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
