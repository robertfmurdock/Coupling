package com.zegreatrob.coupling.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import java.io.File
import java.io.FileOutputStream

open class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @Internal
    lateinit var projectNodeModulesDir: File

    @Internal
    lateinit var nodeBinDir: File

    @Internal
    lateinit var nodeExecPath: String

    @Internal
    var nodeModulesDir: File? = null

    @Input
    @Optional
    var moreNodeDirs: String? = null

    @Internal
    var npmProjectDir: File? = null

    @OutputFile
    @Optional
    var outputFile: File? = null

    @Input
    @Optional
    var nodeCommand: String? = null

    @Input
    var arguments: List<String> = emptyList()

    override fun exec() {
        environment(
            "NODE_PATH",
            listOfNotNull(nodeModulesDir, projectNodeModulesDir, moreNodeDirs)
                .joinToString(":")
        )
        environment("PATH", "$nodeBinDir${System.getenv("PATH")}")
        npmProjectDir?.let { workingDir = it }
        val commandFromBin = nodeCommand?.let { listOf("$projectNodeModulesDir/.bin/$nodeCommand") } ?: emptyList()
        commandLine = listOf(nodeExecPath) + commandFromBin + arguments

        outputFile?.let {
            standardOutput = FileOutputStream(it)
            errorOutput = standardOutput
        }

        super.exec()
    }
}

fun NodeExec.setup(project: Project) {
    val nodeJs = NodeJsRootPlugin.apply(project.rootProject)
    @Suppress("DEPRECATION")
    nodeBinDir = nodeJs.requireConfigured().nodeBinDir
    @Suppress("DEPRECATION")
    nodeExecPath = nodeJs.requireConfigured().executable
    projectNodeModulesDir = project.nodeModulesDir
}


val Project.nodeModulesDir: File get() = rootProject.layout.buildDirectory.dir("js/node_modules").get().asFile
