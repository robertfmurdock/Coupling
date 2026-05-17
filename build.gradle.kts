
import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

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

abstract class SyncAiContextTask : DefaultTask() {
    @get:Internal
    abstract val repoRootDirPath: Property<String>

    @get:Internal
    abstract val settingsFilePath: Property<String>

    @get:Internal
    abstract val adaptersDirPath: Property<String>

    @get:Internal
    abstract val generatedDirPath: Property<String>

    @get:Internal
    abstract val repoIndexFilePath: Property<String>

    @get:Internal
    abstract val workflowsFilePath: Property<String>

    @get:Internal
    abstract val claudeFilePath: Property<String>

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun sync() {
        val generatedDir = File(generatedDirPath.get())
        val adaptersDir = File(adaptersDirPath.get())
        val repoIndexFile = File(repoIndexFilePath.get())
        val workflowsFile = File(workflowsFilePath.get())
        val claudeFile = File(claudeFilePath.get())
        val settingsFile = File(settingsFilePath.get())
        val repoRootDir = File(repoRootDirPath.get())

        generatedDir.mkdirs()

        val includeRegex = Regex("""^include\("([^"]+)"\)""")
        val modules = settingsFile
            .readLines()
            .mapNotNull { line -> includeRegex.find(line.trim())?.groupValues?.get(1) }

        val topLevelDirs = repoRootDir
            .listFiles()
            ?.asSequence()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.map { it.name }
            ?.sorted()
            ?.toList()
            ?: emptyList()

        repoIndexFile.writeText(
            buildString {
                appendLine("# Repo Index (Generated)")
                appendLine()
                appendLine("Source: `settings.gradle.kts` includes + top-level directory layout.")
                appendLine()
                appendLine("## Top-Level Areas")
                topLevelDirs.forEach { appendLine("- `$it/`") }
                appendLine()
                appendLine("## Included Gradle Modules")
                modules.forEach { appendLine("- `$it`") }
            },
        )

        workflowsFile.writeText(
            """
            # Workflows (Generated)

            ## Standard Validation Commands
            - `./gradlew test`
            - `./gradlew build`
            - `./gradlew check`

            ## Scoped Validation Convention
            - `./gradlew :module:task`

            ## GraphQL Ref Workflow
            1. Run `agents.d/utilities/graphql-ref-scan.sh <pattern>` (text-reference discovery only).
            2. Update schema/resolver/sdk/tests in same change set.
            3. Re-run `agents.d/utilities/graphql-ref-scan.sh <pattern>`.
            4. Run targeted module checks, then broader checks if needed.

            ## CI-Relevant Notes
            - Gradle wrapper required.
            - Prefer module-scoped tasks first for faster iteration.
            """.trimIndent() + "\n",
        )

        adaptersDir.resolve("CLAUDE.md").copyTo(claudeFile, overwrite = true)

        logger.lifecycle("Synced AI context files:")
        logger.lifecycle("- CLAUDE.md")
        logger.lifecycle("- agents.d/context/generated/repo-index.md")
        logger.lifecycle("- agents.d/context/generated/workflows.md")
    }
}

abstract class ValidateAiContextManifestTask : DefaultTask() {
    @get:Internal
    abstract val repoRootDirPath: Property<String>

    @get:Internal
    abstract val contextManifestFilePath: Property<String>

    @TaskAction
    fun validate() {
        val manifestFile = File(contextManifestFilePath.get())
        val repoRootDir = File(repoRootDirPath.get())
        val manifest = ObjectMapper().readTree(manifestFile)

        val requiredReads = manifest
            .path("required_reads")
            .takeIf { it.isArray }
            ?.mapNotNull { it.asText(null) }
            ?: emptyList()
        val playbooks = manifest
            .path("playbooks")
            .takeIf { it.isObject }
            ?.properties()
            ?.asSequence()
            ?.mapNotNull { it.value.asText(null) }
            ?.toList()
            ?: emptyList()

        val manifestPaths = (requiredReads + playbooks).distinct()
        val missing = manifestPaths.filterNot { File(repoRootDir, it).exists() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("AI context manifest contains missing files:")
                    missing.forEach { appendLine("- $it") }
                },
            )
        }

        logger.lifecycle("AI context manifest validation passed (${manifestPaths.size} files).")
    }
}

abstract class AgentBootstrapTask : DefaultTask() {
    @get:Internal
    abstract val repoRootDirPath: Property<String>

    @get:Internal
    abstract val contextManifestFilePath: Property<String>

    @TaskAction
    fun bootstrap() {
        val manifestFile = File(contextManifestFilePath.get())
        val repoRootDir = File(repoRootDirPath.get())
        val manifest = ObjectMapper().readTree(manifestFile)
        val requiredReads = manifest
            .path("required_reads")
            .takeIf { it.isArray }
            ?.mapNotNull { it.asText(null) }
            ?: emptyList()
        val playbooks = manifest
            .path("playbooks")
            .takeIf { it.isObject }
            ?.properties()
            ?.asSequence()
            ?.mapNotNull { it.value.asText(null) }
            ?.toList()
            ?: emptyList()
        val readOrder = (requiredReads + playbooks + "agents.d/context/context.json").distinct()

        logger.lifecycle("Agent bootstrap read order:")
        readOrder.forEach { relPath ->
            val marker = if (File(repoRootDir, relPath).exists()) "" else " (missing)"
            logger.lifecycle("- $relPath$marker")
        }
        logger.lifecycle("")
        logger.lifecycle("`./gradlew agentBootstrap` already refreshes generated AI context files.")
    }
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
