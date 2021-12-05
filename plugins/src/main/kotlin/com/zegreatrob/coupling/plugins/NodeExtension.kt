package com.zegreatrob.coupling.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File

abstract class NodeExtension {
    fun Project.getNodeBinDir(): File {
        val props = System.getProperties()
        fun property(name: String) = props.getProperty(name) ?: System.getProperty(name)
        val win = "win"
        val darwin = "darwin"
        val linux = "linux"
        val sunos = "sunos"

        val platform: String = run {
            val name = property("os.name").toLowerCase()
            when {
                name.contains("windows") -> win
                name.contains("mac") -> darwin
                name.contains("linux") -> linux
                name.contains("freebsd") -> linux
                name.contains("sunos") -> sunos
                else -> throw IllegalArgumentException("Unsupported OS: $name")
            }
        }
        val x64 = "x64"
        val x86 = "x86"
        val arm64 = "arm64"

        val architecture: String = run {
            val arch = property("os.arch").toLowerCase()
            when {
                arch.contains("64") -> x64
                arch == "arm" -> {
                    // as Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch
                    // the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here
                    val process = Runtime.getRuntime().exec("uname -m")
                    val systemArch = process.inputStream.bufferedReader().use { it.readText() }
                    when (systemArch.trim()) {
                        "armv8l" -> arm64
                        else -> systemArch
                    }
                }
                else -> x86
            }
        }
        val nodeJs = NodeJsRootPlugin.apply(this)
        val installationDir = nodeJs.installationDir
        val nodeDir = installationDir.resolve("node-v${nodeJs.nodeVersion}-$platform-$architecture")

        val isWindows = platform == win
        return if (isWindows) nodeDir else nodeDir.resolve("bin")
    }

    val Project.nodeModulesDir get() = rootProject.buildDir.resolve("js/node_modules")

    val Exec.nodeExecPath get() = "${nodeBinDir}/node"

    val Exec.nodeBinDir get() = project.rootProject.getNodeBinDir()

    fun Exec.nodeExec(compileKotlinJs: Kotlin2JsCompile, arguments: List<String>) {
        dependsOn(compileKotlinJs)
        environment("NODE_PATH", project.nodeModulesDir)
        commandLine = listOf(nodeExecPath) + arguments
    }

}

open class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    @Input
    var nodeCommand: String? = null

    @Input
    var arguments: List<String> = emptyList()

    override fun exec() {
        environment("NODE_PATH", project.nodeModulesDir)
        environment("PATH", "$nodeBinDir")
        val commandFromBin = nodeCommand?.let { listOf("${project.nodeModulesDir}/.bin/$nodeCommand") } ?: emptyList()
        commandLine = listOf(nodeExecPath) + commandFromBin + arguments
        super.exec()
    }


    private fun Project.getNodeBinDir(): File {
        val props = System.getProperties()
        fun property(name: String) = props.getProperty(name) ?: System.getProperty(name)
        val win = "win"
        val darwin = "darwin"
        val linux = "linux"
        val sunos = "sunos"

        val platform: String = run {
            val name = property("os.name").toLowerCase()
            when {
                name.contains("windows") -> win
                name.contains("mac") -> darwin
                name.contains("linux") -> linux
                name.contains("freebsd") -> linux
                name.contains("sunos") -> sunos
                else -> throw IllegalArgumentException("Unsupported OS: $name")
            }
        }
        val x64 = "x64"
        val x86 = "x86"
        val arm64 = "arm64"

        val architecture: String = run {
            val arch = property("os.arch").toLowerCase()
            when {
                arch.contains("64") -> x64
                arch == "arm" -> {
                    // as Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch
                    // the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here
                    val process = Runtime.getRuntime().exec("uname -m")
                    val systemArch = process.inputStream.bufferedReader().use { it.readText() }
                    when (systemArch.trim()) {
                        "armv8l" -> arm64
                        else -> systemArch
                    }
                }
                else -> x86
            }
        }
        val nodeJs = NodeJsRootPlugin.apply(this)
        val installationDir = nodeJs.installationDir
        val nodeDir = installationDir.resolve("node-v${nodeJs.nodeVersion}-$platform-$architecture")

        val isWindows = platform == win
        return if (isWindows) nodeDir else nodeDir.resolve("bin")
    }

    private val Project.nodeModulesDir get() = rootProject.buildDir.resolve("js/node_modules")

    private val nodeExecPath get() = "${nodeBinDir}/node"

    private val nodeBinDir get() = project.rootProject.getNodeBinDir()

}