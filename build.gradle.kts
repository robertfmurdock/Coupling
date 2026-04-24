
import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.tasks.Exec
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
    val analyzeTaskFlags = providers.provider {
        buildList {
            val strict = providers
                .gradleProperty("coupling.testLog.analyze.strict")
                .map { it.equals("true", ignoreCase = true) }
                .getOrElse(false)
            if (strict) {
                add("--strict")
            }
        }
    }

    val validateTestJsonl by registering(Exec::class) {
        group = "verification"
        description = "Validates build/test-output/test.jsonl for minimum required schema."
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

    val validateTestJsonlKotlin by registering {
        group = "verification"
        description = "Compatibility alias for validateTestJsonl during Kotlin migration."
        dependsOn(validateTestJsonl)
    }

    val analyzeTestJsonl by registering(Exec::class) {
        group = "verification"
        description = "Analyzes test coverage and TestMints phase logging in build/test-output/test.jsonl."
        notCompatibleWithConfigurationCache("Resolves CLI runtime classpath dynamically for a helper migration task.")
        dependsOn(":cli:test-log-tools:jvmJar")
        doFirst {
            commandLine(
                listOf(
                    "java",
                    "-cp",
                    testLogToolsClasspath.get(),
                    "com.zegreatrob.coupling.cli.testlog.MainKt",
                    "analyze",
                ) + analyzeTaskFlags.get() + listOf(testJsonlFilePath.get()),
            )
        }
    }

    val analyzeTestJsonlKotlin by registering {
        group = "verification"
        description = "Compatibility alias for analyzeTestJsonl during Kotlin migration."
        dependsOn(analyzeTestJsonl)
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
