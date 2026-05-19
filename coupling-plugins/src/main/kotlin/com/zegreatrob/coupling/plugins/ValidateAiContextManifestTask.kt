package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

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
        val playbookPaths = manifest
            .path("playbooks")
            .takeIf { it.isObject }
            ?.properties()
            ?.asSequence()
            ?.mapNotNull { (_, node) -> node.path("path").asText(null) }
            ?.toList()
            ?: emptyList()

        val manifestPaths = (requiredReads + playbookPaths).distinct()
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
