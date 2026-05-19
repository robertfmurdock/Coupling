package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SyncAiContextTask : DefaultTask() {
    @get:Internal
    abstract val repoRootDirPath: Property<String>

    @get:InputFile
    abstract val settingsFilePath: Property<String>

    @get:InputDirectory
    abstract val adaptersDirPath: Property<String>

    @get:Internal
    abstract val generatedDirPath: Property<String>

    @get:OutputFile
    abstract val repoIndexFilePath: Property<String>

    @get:OutputFile
    abstract val workflowsFilePath: Property<String>

    @get:OutputFile
    abstract val claudeFilePath: Property<String>

    @get:OutputFile
    abstract val copilotFilePath: Property<String>

    @get:InputFile
    abstract val contextManifestFilePath: Property<String>

    @TaskAction
    fun sync() {
        val generatedDir = File(generatedDirPath.get())
        val adaptersDir = File(adaptersDirPath.get())
        val repoIndexFile = File(repoIndexFilePath.get())
        val workflowsFile = File(workflowsFilePath.get())
        val claudeFile = File(claudeFilePath.get())
        val copilotFile = File(copilotFilePath.get())
        val settingsFile = File(settingsFilePath.get())
        val repoRootDir = File(repoRootDirPath.get())
        val manifestFile = File(contextManifestFilePath.get())

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

        val playbookBullets = playbookBullets(manifestFile)
        val requiredReadsBullets = requiredReadsBullets(manifestFile)
        val commandsBullets = commandsBullets(manifestFile)
        val executionNormsBullets = executionNormsBullets(manifestFile)
        val rulesBullets = rulesBullets(manifestFile)

        fun applyTemplate(adapterName: String, outputFile: File) {
            val template = adaptersDir.resolve(adapterName).readText()
            outputFile.writeText(
                template
                    .replace("{{PLAYBOOKS}}", playbookBullets)
                    .replace("{{REQUIRED_READS}}", requiredReadsBullets)
                    .replace("{{COMMANDS}}", commandsBullets)
                    .replace("{{EXECUTION_NORMS}}", executionNormsBullets)
                    .replace("{{RULES}}", rulesBullets),
            )
        }

        applyTemplate("CLAUDE.md", claudeFile)
        applyTemplate("copilot-instructions.md", copilotFile)

        logger.lifecycle("Synced AI context files:")
        logger.lifecycle("- CLAUDE.md")
        logger.lifecycle("- .github/copilot-instructions.md")
        logger.lifecycle("- agents.d/context/generated/repo-index.md")
        logger.lifecycle("- agents.d/context/generated/workflows.md")
    }

    private fun playbookBullets(manifestFile: File): String {
        val manifest = ObjectMapper().readTree(manifestFile)
        return manifest.path("playbooks")
            .takeIf { it.isObject }
            ?.properties()
            ?.asSequence()
            ?.mapNotNull { (_, node) ->
                val path = node.path("path").asText(null) ?: return@mapNotNull null
                val whenText = node.path("when").asText(null) ?: return@mapNotNull null
                "- `$path` — $whenText"
            }
            ?.joinToString("\n")
            ?: ""
    }

    private fun requiredReadsBullets(manifestFile: File): String {
        val manifest = ObjectMapper().readTree(manifestFile)
        return manifest.path("required_reads")
            .takeIf { it.isArray }
            ?.mapNotNull { it.asText(null) }
            ?.joinToString("\n") { "- `$it`" }
            ?: ""
    }

    private fun commandsBullets(manifestFile: File): String {
        val manifest = ObjectMapper().readTree(manifestFile)
        val commands = manifest.path("commands")

        val bullets = buildList {
            commands.path("agent_bootstrap").asText(null)?.let { add("- `$it`") }
            commands.path("default")
                .takeIf { it.isArray }
                ?.mapNotNull { it.asText(null) }
                ?.forEach { add("- `$it`") }
            commands.path("module_task_pattern").asText(null)?.let { add("- `$it`") }
        }

        return bullets.joinToString("\n")
    }

    private fun executionNormsBullets(manifestFile: File): String {
        val manifest = ObjectMapper().readTree(manifestFile)
        val commands = manifest.path("commands")
        val adaptersDir = File(adaptersDirPath.get())
        val template = adaptersDir.resolve("execution-norms.md").readText()

        return template
            .replace("{{AGENT_BOOTSTRAP}}", commands.path("agent_bootstrap").asText(""))
    }

    private fun rulesBullets(manifestFile: File): String {
        val manifest = ObjectMapper().readTree(manifestFile)
        val commands = manifest.path("commands")
        val adaptersDir = File(adaptersDirPath.get())
        val template = adaptersDir.resolve("rules.md").readText()

        return template
            .replace("{{AGENT_BOOTSTRAP}}", commands.path("agent_bootstrap").asText(""))
            .replace("{{GRAPHQL_REF_SCAN}}", commands.path("graphql_ref_scan").asText(""))
    }
}
