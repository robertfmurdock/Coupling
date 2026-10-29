package com.zegreatrob.coupling.plugins.js

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import java.io.FileOutputStream

abstract class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @get:Internal
    abstract val projectNodeModulesDir: DirectoryProperty

    @get:Internal
    abstract val nodeBinDir: DirectoryProperty

    @get:Internal
    abstract val nodeExecPath: Property<String>

    @get:Internal
    abstract val nodeModulesDir: DirectoryProperty

    @get:Input
    @Optional
    var moreNodeDirs: String? = null

    @get:Internal
    abstract val npmProjectDir: DirectoryProperty

    @get:Internal
    abstract val taskWorkingDirectory: DirectoryProperty

    @get:OutputFile
    @get:Optional
    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val nodeCommand: Property<String>

    @get:Input
    abstract val arguments: ListProperty<String>

    override fun exec() {
        environment(
            "NODE_PATH",
            listOfNotNull(nodeModulesDir.orNull, projectNodeModulesDir.orNull)
                .map { it.asFile }
                .plus(listOfNotNull(moreNodeDirs))
                .joinToString(":"),
        )
        environment("PATH", "${nodeBinDir.get().asFile}${System.getenv("PATH")}")
        (taskWorkingDirectory.orNull ?: npmProjectDir.orNull)?.let { workingDir = it.asFile }
        val commandFromBin = nodeCommand.orNull
            ?.let { listOf(projectNodeModulesDir.get().file(".bin/$it").asFile.absolutePath) }
            ?: emptyList()
        commandLine = listOf(nodeExecPath.get()) + commandFromBin + arguments.get()

        outputFile.orNull?.asFile?.let {
            standardOutput = FileOutputStream(it)
            errorOutput = standardOutput
        }

        super.exec()
    }
}

@CacheableTask
abstract class CacheableNodeExec : NodeExec()

fun NodeExec.setup(project: Project) {
    val nodeJs = NodeJsRootPlugin.apply(project.rootProject)
    @Suppress("DEPRECATION")
    nodeBinDir.set(nodeJs.requireConfigured().nodeBinDir)
    @Suppress("DEPRECATION")
    nodeExecPath.set(nodeJs.requireConfigured().executable)
    projectNodeModulesDir.set(project.nodeModulesDir)
}

val Project.nodeModulesDir get() = rootProject.layout.buildDirectory.dir("js/node_modules")
