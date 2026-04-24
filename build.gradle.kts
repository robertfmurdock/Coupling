
import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.tasks.Exec
import java.io.ByteArrayOutputStream
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
    add(testLogToolsRunner.name, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0-rc01")
    add(testLogToolsRunner.name, project(":cli:test-log-tools"))
}

tasks {
    val testJsonlFilePath = rootProject.layout.buildDirectory.file("test-output/test.jsonl").map { it.asFile.absolutePath }
    val testLogToolsClasspath = providers.provider {
        testLogToolsRunner
            .resolve()
            .joinToString(File.pathSeparator) { it.absolutePath }
    }
    val validateTaskFlags = providers.provider {
        buildList {
            val failOnNonJsonEnabled = providers
                .gradleProperty("coupling.testLog.failNonJson")
                .map { !it.equals("false", ignoreCase = true) }
                .getOrElse(true)
            val failOnMissingCoreEnabled = providers
                .gradleProperty("coupling.testLog.failMissingCore")
                .map { !it.equals("false", ignoreCase = true) }
                .getOrElse(true)
            if (failOnNonJsonEnabled) {
                add("--fail-on-non-json")
            }
            if (failOnMissingCoreEnabled) {
                add("--fail-on-missing-core")
            }
        }
    }

    val validateTestJsonl by registering(Exec::class) {
        group = "verification"
        description = "Validates build/test-output/test.jsonl for minimum required schema."
        val baseCommand = mutableListOf<String>().apply {
            addAll(
                listOf(
            "node",
            "scripts/validate-test-jsonl.mjs"
                ),
            )
            addAll(validateTaskFlags.get())
            add(testJsonlFilePath.get())
        }
        commandLine(baseCommand)
    }

    val validateTestJsonlKotlin by registering(Exec::class) {
        group = "verification"
        description = "Validates build/test-output/test.jsonl using the Kotlin test-log tool."
        notCompatibleWithConfigurationCache("Resolves CLI runtime classpath dynamically for a helper migration task.")
        dependsOn(":cli:test-log-tools:jvmJar")
        doFirst {
            commandLine(
                listOf(
                    "java",
                    "-cp",
                    testLogToolsClasspath.get(),
                    "com.zegreatrob.coupling.cli.testlog.MainKt",
                    "validate",
                ) + validateTaskFlags.get() + listOf(testJsonlFilePath.get()),
            )
        }
    }

    val validateTestJsonlParity by registering {
        group = "verification"
        description = "Runs JS and Kotlin validators and fails when key metrics diverge."
        notCompatibleWithConfigurationCache("Uses ProcessBuilder to capture command output for parity comparison.")
        val parityKeys = listOf(
            "total_lines",
            "non_empty_lines",
            "parsed_json_lines",
            "non_json_lines",
            "missing_core_fields",
            "missing_end_fields",
            "bad_duration_ms",
            "total_violations",
            "failing_violations",
            "mode",
        )
        doLast {
            val jsCommand = listOf("node", "scripts/validate-test-jsonl.mjs") +
                validateTaskFlags.get() +
                listOf(testJsonlFilePath.get())
            val jsStdout = ByteArrayOutputStream()
            val jsResult = ProcessBuilder(jsCommand)
                .directory(rootProject.projectDir)
                .redirectErrorStream(true)
                .start()
                .also { process ->
                    process.inputStream.copyTo(jsStdout)
                }
                .waitFor()

            val kotlinCommand = listOf(
                "java",
                "-cp",
                testLogToolsClasspath.get(),
                "com.zegreatrob.coupling.cli.testlog.MainKt",
                "validate",
            ) + validateTaskFlags.get() + listOf(testJsonlFilePath.get())
            val kotlinStdout = ByteArrayOutputStream()
            val kotlinResult = ProcessBuilder(kotlinCommand)
                .directory(rootProject.projectDir)
                .redirectErrorStream(true)
                .start()
                .also { process ->
                    process.inputStream.copyTo(kotlinStdout)
                }
                .waitFor()

            val mapper = ObjectMapper()
            val jsReport = mapper.readTree(jsStdout.toString("UTF-8"))
            val kotlinReport = mapper.readTree(kotlinStdout.toString("UTF-8"))
            val mismatches = parityKeys.mapNotNull { key ->
                val jsValue = jsReport.get(key)
                val kotlinValue = kotlinReport.get(key)
                if (jsValue == kotlinValue) {
                    null
                } else {
                    "$key js=${jsValue ?: "null"} kotlin=${kotlinValue ?: "null"}"
                }
            }

            val exitCodeMismatch = jsResult != kotlinResult
            if (exitCodeMismatch || mismatches.isNotEmpty()) {
                val message = buildString {
                    appendLine("validateTestJsonlParity mismatch detected.")
                    appendLine("js_exit=$jsResult kotlin_exit=$kotlinResult")
                    if (mismatches.isNotEmpty()) {
                        appendLine("metric_mismatches:")
                        mismatches.forEach { appendLine("  - $it") }
                    }
                }
                throw GradleException(message)
            }

            logger.lifecycle("validateTestJsonlParity matched for keys=${parityKeys.joinToString(",")}")
        }
    }

    val analyzeTestJsonl by registering(Exec::class) {
        group = "verification"
        description = "Analyzes test coverage and TestMints phase logging in build/test-output/test.jsonl."
        val command = mutableListOf(
            "node",
            "scripts/analyze-test-jsonl.mjs"
        )
        val strict = providers
            .gradleProperty("coupling.testLog.analyze.strict")
            .map { it.equals("true", ignoreCase = true) }
            .getOrElse(false)
        if (strict) {
            command.add("--strict")
        }
        command.add(rootProject.layout.buildDirectory.file("test-output/test.jsonl").get().asFile.absolutePath)
        commandLine(command)
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
