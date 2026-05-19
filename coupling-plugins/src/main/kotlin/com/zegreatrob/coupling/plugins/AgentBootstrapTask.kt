package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

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
            ?.mapNotNull { (_, node) ->
                val path = node.path("path").asText(null) ?: return@mapNotNull null
                val whenText = node.path("when").asText(null) ?: ""
                path to whenText
            }
            ?.toList()
            ?: emptyList()

        logger.lifecycle("Agent bootstrap read order:")
        logger.lifecycle("")
        logger.lifecycle("Required reads:")
        (requiredReads + "agents.d/context/context.json").forEach { relPath ->
            val marker = if (File(repoRootDir, relPath).exists()) "" else " (MISSING)"
            logger.lifecycle("  - $relPath$marker")
        }
        logger.lifecycle("")
        logger.lifecycle("Conditional reads (load the matching playbook for your task type):")
        playbooks.forEach { (path, whenText) ->
            val marker = if (File(repoRootDir, path).exists()) "" else " (MISSING)"
            logger.lifecycle("  - $path — $whenText$marker")
        }
        logger.lifecycle("")
        logger.lifecycle("`./gradlew agentBootstrap` already refreshes generated AI context files.")
    }
}
