
import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.GradleException
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.time.Duration

plugins {
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.linter")
    alias(libs.plugins.com.avast.gradle.docker.compose)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.com.zegreatrob.tools.digger)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.apollographql.apollo) apply false
    base
}

dockerCompose {
    setProjectName("Coupling-root")
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    startedServices.set(listOf("serverless", "caddy", "dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
    waitAfterHealthyStateProbeFailure.set(Duration.ofMillis(100))
    val (sak, pk, sk) = providers.exec {
        commandLine(
            "/bin/bash",
            "-c",
            "aws ssm get-parameters --names /local/SERVERLESS_ACCESS_KEY /prerelease/stripe_pk /prerelease/stripe_sk --with-decryption | jq '[.Parameters[].Value']"
        )
    }.standardOutput.asText.get().toByteArray().let { ObjectMapper().readValue(it, List::class.java) }
    environment.put("SERVERLESS_ACCESS_KEY", sak.toString())
    environment.put("STRIPE_PUBLISHABLE_KEY", pk.toString())
    environment.put("STRIPE_SECRET_KEY", sk.toString())

    nested("caddy").apply {
        setProjectName("Coupling-root")
        startedServices.set(listOf("caddy"))
        waitForTcpPorts.set(false)
    }
}

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

val testLogToolsRunner by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    add(testLogToolsRunner.name, enforcedPlatform(project(":libraries:dependency-bom")))
    add(testLogToolsRunner.name, "org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    add(testLogToolsRunner.name, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0-rc02")
    add(testLogToolsRunner.name, project(":cli:test-log-tools"))
}

tasks {
    val testJsonlFilePath = rootProject.layout.buildDirectory.file("test-output/test.jsonl").map { it.asFile.absolutePath }
    val validateReportFilePath = rootProject.layout.buildDirectory.file("reports/test-logs/validate-test-jsonl.json").map { it.asFile.absolutePath }
    val analyzeReportFilePath = rootProject.layout.buildDirectory.file("reports/test-logs/analyze-test-jsonl.json").map { it.asFile.absolutePath }
    val testLogToolsClasspath = providers.provider {
        testLogToolsRunner
            .resolve()
            .joinToString(File.pathSeparator) { it.absolutePath }
    }
    fun registerTestLogCliTask(
        name: String,
        command: String,
        reportFilePath: () -> String,
        descriptionText: String,
        strictFlags: List<String> = listOf("--strict"),
    ): TaskProvider<Exec> = register<Exec>(name) {
        group = "verification"
        description = descriptionText
        notCompatibleWithConfigurationCache("Resolves CLI runtime classpath dynamically for a helper migration task.")
        dependsOn(":cli:test-log-tools:jvmJar")
        doFirst {
            commandLine(
                buildList {
                    addAll(
                        listOf(
                            "java",
                            "-cp",
                            testLogToolsClasspath.get(),
                            "com.zegreatrob.coupling.cli.testlog.MainKt",
                            command,
                            "--report-file",
                            reportFilePath(),
                            "--quiet-success",
                            "--failure-summary",
                        ),
                    )
                    addAll(strictFlags)
                    add(testJsonlFilePath.get())
                },
            )
        }
    }

    val validateTestJsonl = registerTestLogCliTask(
        name = "validateTestJsonl",
        command = "validate",
        reportFilePath = { validateReportFilePath.get() },
        descriptionText = "Validates build/test-output/test.jsonl for minimum required schema.",
    )

    val analyzeTestJsonl = registerTestLogCliTask(
        name = "analyzeTestJsonl",
        command = "analyze",
        reportFilePath = { analyzeReportFilePath.get() },
        descriptionText = "Analyzes test coverage and TestMints phase logging in build/test-output/test.jsonl.",
    )

    data class AttributionCoverage(
        val inScope: Int,
        val fullyAttributed: Int,
        val missingAny: Int,
        val ratio: Double,
    )

    fun readAttributionCoverage(reportFilePath: String): AttributionCoverage {
        val reportFile = File(reportFilePath)
        if (!reportFile.exists()) {
            throw GradleException("analyze report file not found: $reportFilePath")
        }
        val report = ObjectMapper().readTree(reportFile)
        return AttributionCoverage(
            inScope = report.get("command_events_in_attribution_scope")?.asInt() ?: 0,
            fullyAttributed = report.get("command_events_with_full_test_attribution")?.asInt() ?: 0,
            missingAny = report.get("command_events_missing_any_test_attribution")?.asInt() ?: 0,
            ratio = report.get("command_events_with_full_test_attribution_ratio")?.asDouble() ?: 0.0,
        )
    }

    val assertCommandAttributionCoverage by registering {
        group = "verification"
        description = "Asserts command logs are 100% test-attributed for attribution-required tasks."
        dependsOn(analyzeTestJsonl)
        doLast {
            val coverage = readAttributionCoverage(analyzeReportFilePath.get())
            if (coverage.missingAny > 0 || (coverage.inScope > 0 && coverage.ratio < 1.0)) {
                throw GradleException(
                    "command attribution coverage check failed: in_scope=${coverage.inScope} fully_attributed=${coverage.fullyAttributed} missing_any=${coverage.missingAny} ratio=${coverage.ratio}",
                )
            }
            logger.lifecycle(
                "command attribution coverage check passed: in_scope=${coverage.inScope} fully_attributed=${coverage.fullyAttributed} missing_any=${coverage.missingAny} ratio=${coverage.ratio}",
            )
        }
    }

    check {
        dependsOn(project.getTasksByName("check", true).filterNot { it.project == this.project })
        finalizedBy(validateTestJsonl)
    }
    named<ComposeUp>("composeUp") {
        mustRunAfter("caddyComposeUp", "libraries:repository:dynamo:composeUp")
        dependsOn(":server:buildImage")
    }
    "versionCatalogUpdate" {
        dependsOn("libraries:js-dependencies:ncuUpgrade")
        dependsOn(provider { gradle.includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }
    release {
        finalizedBy(currentContributionData)
    }
}
