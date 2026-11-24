package com.zegreatrob.coupling.plugins.js

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class NpmPackTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val packageDirectory: DirectoryProperty

    @get:OutputFile
    abstract val archiveFile: RegularFileProperty

    @get:Input
    abstract val packageVersion: Property<String>

    @TaskAction
    fun pack() {
        val manifest = packageDirectory.file("package.json").get().asFile
        val actualVersion = ObjectMapper().readTree(manifest)["version"].asText()
        check(actualVersion == packageVersion.get()) {
            "Generated npm manifest version $actualVersion differs from requested ${packageVersion.get()}. " +
                "Rebuild :cli:jsPackageJson before packing; a stale package cannot be published."
        }
        val output = ByteArrayOutputStream()
        execOperations.exec {
            workingDir(packageDirectory)
            commandLine("npm", "pack", "--json", "--pack-destination", temporaryDir.absolutePath)
            standardOutput = output
        }
        val filename = ObjectMapper().readTree(output.toString()).single()["filename"].asText()
        val archive = archiveFile.get().asFile
        archive.parentFile.mkdirs()
        temporaryDir.resolve(filename).copyTo(archive, overwrite = true)
    }
}

abstract class VerifyNpmPackageTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val archiveFile: RegularFileProperty

    @TaskAction
    fun verify() {
        temporaryDir.deleteRecursively()
        val consumer = temporaryDir.resolve("consumer").apply { mkdirs() }
        consumer.resolve("package.json").writeText("""{"private":true}""")
        execOperations.exec {
            workingDir(consumer)
            commandLine("npm", "install", "--no-audit", "--no-fund", archiveFile.get().asFile.absolutePath)
        }
        val manifest = consumer.resolve("node_modules/@continuous-excellence/coupling-cli/package.json")
        val version = ObjectMapper().readTree(manifest)["version"].asText()
        val unrelatedDirectory = temporaryDir.resolve("elsewhere").apply { mkdirs() }
        listOf("--version", "--help").forEach { argument ->
            val output = ByteArrayOutputStream()
            execOperations.exec {
                workingDir(unrelatedDirectory)
                environment = environment.filterKeys { !it.startsWith("COUPLING_") }
                environment("COUPLING_VERSION", "must-not-override-installed-version")
                commandLine(consumer.resolve("node_modules/.bin/coupling").absolutePath, argument)
                standardOutput = output
            }
            val text = output.toString().trim()
            if (argument == "--version") {
                check(text == "coupling-cli version $version") { "Installed CLI must report manifest version $version, got: $text" }
            } else {
                check(text.contains("Usage:") && text.contains("--help")) { "Installed CLI did not display help: $text" }
            }
        }
    }
}

abstract class NpmPublishTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val archiveFile: RegularFileProperty

    @TaskAction
    fun publish() {
        execOperations.exec {
            commandLine("npm", "publish", archiveFile.get().asFile.absolutePath)
        }
    }
}
