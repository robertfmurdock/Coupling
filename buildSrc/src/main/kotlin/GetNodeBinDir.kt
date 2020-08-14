package com.zegreatrob.coupling.build

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.File


fun Project.getNodeBinDir(): File {
    val props = System.getProperties()
    fun property(name: String) = props.getProperty(name) ?: System.getProperty(name)
    val WIN = "win"
    val DARWIN = "darwin"
    val LINUX = "linux"
    val SUNOS = "sunos"

    val platform: String = run {
        val name = property("os.name").toLowerCase()
        when {
            name.contains("windows") -> WIN
            name.contains("mac") -> DARWIN
            name.contains("linux") -> LINUX
            name.contains("freebsd") -> LINUX
            name.contains("sunos") -> SUNOS
            else -> throw IllegalArgumentException("Unsupported OS: $name")
        }
    }
    val X64 = "x64"
    val X86 = "x86"
    val ARM64 = "arm64"

    val architecture: String = run {
        val arch = property("os.arch").toLowerCase()
        when {
            arch.contains("64") -> X64
            arch == "arm" -> {
                // as Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch
                // the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here
                val process = Runtime.getRuntime().exec("uname -m")
                val systemArch = process.inputStream.bufferedReader().use { it.readText() }
                when (systemArch.trim()) {
                    "armv8l" -> ARM64
                    else -> systemArch
                }
            }
            else -> X86
        }
    }
    val nodeJs = NodeJsRootPlugin.apply(this)
    val installationDir = nodeJs.installationDir
    val nodeDir = installationDir.resolve("node-v${nodeJs.nodeVersion}-$platform-$architecture")

    val isWindows = platform == WIN
    return if (isWindows) nodeDir else nodeDir.resolve("bin")
}

val Project.nodeModulesDir get() = "${rootProject.buildDir.resolve("js/node_modules")}"

val Exec.nodeExecPath get() = "${nodeBinDir}/node"

val Exec.nodeBinDir get() = project.rootProject.getNodeBinDir()

fun Exec.nodeExec(compileKotlinJs: Kotlin2JsCompile, arguments: List<String>) {
    dependsOn(compileKotlinJs)
    environment("NODE_PATH", project.nodeModulesDir)
    commandLine = listOf(nodeExecPath) + arguments
}