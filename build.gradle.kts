import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.zegreatrob.coupling.plugins.AgentBootstrapTask
import com.zegreatrob.coupling.plugins.SyncAiContextTask
import com.zegreatrob.coupling.plugins.ValidateAiContextManifestTask
import com.zegreatrob.coupling.plugins.fetchAwsSsmParameters
import com.zegreatrob.coupling.plugins.readAttributionCoverage
import com.zegreatrob.coupling.plugins.registerTestLogCliTask
import java.time.Duration
import org.gradle.api.GradleException

plugins {
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.linter")
    alias(libs.plugins.com.avast.gradle.docker.compose)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.com.zegreatrob.tools.digger)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.apollographql.apollo) apply false
    java
}

dockerCompose {
    setProjectName("Coupling-root")
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    startedServices.set(listOf("serverless", "caddy", "dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
    waitAfterHealthyStateProbeFailure.set(Duration.ofMillis(100))
    val awsParams = providers.fetchAwsSsmParameters()
    environment.put("SERVERLESS_ACCESS_KEY", awsParams.serverlessAccessKey)
    environment.put("STRIPE_PUBLISHABLE_KEY", awsParams.stripePublishableKey)
    environment.put("STRIPE_SECRET_KEY", awsParams.stripeSecretKey)

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
    attributes {
        attribute(Attribute.of("org.jetbrains.kotlin.platform.type", String::class.java), "jvm")
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 22)
    }
}

dependencies {
    add(testLogToolsRunner.name, enforcedPlatform(project(":libraries:dependency-bom")))
    add(testLogToolsRunner.name, "org.jetbrains.kotlinx:kotlinx-serialization-core")
    add(testLogToolsRunner.name, "org.jetbrains.kotlinx:kotlinx-coroutines-core")
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

    val validateTestJsonl = project.registerTestLogCliTask(
        name = "validateTestJsonl",
        command = "validate",
        reportFilePath = { validateReportFilePath.get() },
        descriptionText = "Validates build/test-output/test.jsonl for minimum required schema.",
        testJsonlFilePath = { testJsonlFilePath.get() },
        testLogToolsClasspath = { testLogToolsClasspath.get() },
    )

    val analyzeTestJsonl = project.registerTestLogCliTask(
        name = "analyzeTestJsonl",
        command = "analyze",
        reportFilePath = { analyzeReportFilePath.get() },
        descriptionText = "Analyzes test coverage and TestMints phase logging in build/test-output/test.jsonl.",
        testJsonlFilePath = { testJsonlFilePath.get() },
        testLogToolsClasspath = { testLogToolsClasspath.get() },
    )

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

    val aiContextDir = rootProject.file("agents.d/context")
    val aiAdaptersDir = aiContextDir.resolve("adapters")
    val aiGeneratedDir = aiContextDir.resolve("generated")
    val aiRepoIndexFile = aiGeneratedDir.resolve("repo-index.md")
    val aiWorkflowsFile = aiGeneratedDir.resolve("workflows.md")
    val aiContextManifestFile = aiContextDir.resolve("context.json")
    val aiClaudeFile = rootProject.file("CLAUDE.md")

    val syncAiContext = register<SyncAiContextTask>("syncAiContext") {
        group = "documentation"
        description = "Regenerates AI context generated files and syncs agent adapter outputs."
        repoRootDirPath.set(rootProject.rootDir.absolutePath)
        settingsFilePath.set(rootProject.file("settings.gradle.kts").absolutePath)
        adaptersDirPath.set(aiAdaptersDir.absolutePath)
        generatedDirPath.set(aiGeneratedDir.absolutePath)
        repoIndexFilePath.set(aiRepoIndexFile.absolutePath)
        workflowsFilePath.set(aiWorkflowsFile.absolutePath)
        claudeFilePath.set(aiClaudeFile.absolutePath)
        copilotFilePath.set(rootProject.file(".github/copilot-instructions.md").absolutePath)
        contextManifestFilePath.set(aiContextManifestFile.absolutePath)
    }

    register<AgentBootstrapTask>("agentBootstrap") {
        group = "help"
        description = "Prints the required AI agent context read order."
        dependsOn(syncAiContext)
        repoRootDirPath.set(rootProject.rootDir.absolutePath)
        contextManifestFilePath.set(aiContextManifestFile.absolutePath)
    }

    val validateAiContextManifest by register<ValidateAiContextManifestTask>("validateAiContextManifest") {
        group = "verification"
        description = "Validates agents.d/context/context.json entries point to existing files."
        repoRootDirPath.set(rootProject.rootDir.absolutePath)
        contextManifestFilePath.set(aiContextManifestFile.absolutePath)
    }


    assemble {
        dependsOn(syncAiContext)
    }

    check {
        dependsOn(syncAiContext)
        dependsOn(validateAiContextManifest)
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
