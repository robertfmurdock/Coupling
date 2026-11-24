package com.zegreatrob.coupling.plugins.js

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.io.File
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

@DisableCachingByDefault(because = "Every attempt reads current npm registry state and may publish.")
abstract class NpmPublishTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val archiveFile: RegularFileProperty

    @get:Input
    abstract val npmCommand: ListProperty<String>

    @get:Input
    abstract val publishChanges: Property<Boolean>

    @get:Internal
    abstract val workingDirectory: DirectoryProperty

    init {
        npmCommand.convention(listOf("npm"))
        publishChanges.convention(true)
    }

    @TaskAction
    fun publish() {
        val candidate = archiveFile.get().asFile
        val contents = readNpmPackage(candidate)
        val baselineVersion = latestVersion(contents.name)
        logger.lifecycle("npm baseline: ${contents.name}@$baselineVersion")
        val baseline = readNpmPackage(fetchBaseline(contents.name, baselineVersion))
        check(baseline.name == contents.name && baseline.version == baselineVersion) {
            "Fetched npm baseline does not match ${contents.name}@$baselineVersion"
        }
        val changedPaths = contents.changedPaths(baseline)
        if (changedPaths.isEmpty()) {
            logger.lifecycle("npm package unchanged; skipping publication")
        } else {
            logger.lifecycle("npm package changed ({} paths): {}", changedPaths.size, changedPaths.take(10).joinToString())
            if (publishChanges.get()) npm(listOf("publish", candidate.absolutePath))
        }
    }

    private fun latestVersion(name: String): String {
        val response = npmJsonMapper().readTree(npm(listOf("view", "$name@latest", "version", "--json", "--prefer-online")))
        check(response?.isTextual == true && validNpmVersion(response.asText())) {
            "Unable to resolve an exact npm latest version for $name"
        }
        return response.asText()
    }

    private fun fetchBaseline(name: String, version: String): File {
        val destination = temporaryDir.resolve("baseline").apply {
            deleteRecursively()
            mkdirs()
        }
        val response = npmJsonMapper().readTree(
            npm(listOf("pack", "$name@$version", "--ignore-scripts", "--json", "--pack-destination", destination.absolutePath)),
        )
        check(response?.isArray == true && response.size() == 1) { "Invalid npm pack response for $name@$version" }
        val filename = response.single()["filename"]
        check(filename?.isTextual == true && filename.asText().matches(Regex("[A-Za-z0-9._-]+\\.tgz"))) {
            "Invalid npm baseline archive filename for $name@$version"
        }
        return destination.resolve(filename.asText())
    }

    private fun npm(arguments: List<String>): String {
        val output = ByteArrayOutputStream()
        val npmWorkingDirectory = workingDirectory.get().asFile
        try {
            execOperations.exec {
                workingDir(npmWorkingDirectory)
                commandLine(npmCommand.get() + arguments)
                standardOutput = output
            }
        } catch (failure: Exception) {
            throw GradleException("npm ${arguments.first()} failed during CLI publication/comparison", failure)
        }
        return output.toString()
    }
}

fun Project.registerCliNpmPublication(
    artifact: TaskProvider<NpmPackTask>,
    verification: TaskProvider<VerifyNpmPackageTask>,
    snapshot: Boolean,
): TaskProvider<NpmPublishTask> {
    val publication = tasks.register("jsPublish", NpmPublishTask::class.java) {
        dependsOn(verification)
        archiveFile.set(artifact.flatMap { it.archiveFile })
        workingDirectory.set(artifact.flatMap { it.packageDirectory })
        enabled = !snapshot
        mustRunAfter("check", ":release", ":deploy:prod:deploy")
    }
    tasks.register("compareNpmPackage", NpmPublishTask::class.java) {
        dependsOn(verification)
        archiveFile.set(artifact.flatMap { it.archiveFile })
        workingDirectory.set(artifact.flatMap { it.packageDirectory })
        publishChanges.set(false)
        group = "verification"
        description = "Compare the verified CLI npm artifact with exact npm latest without publishing."
    }
    tasks.register("publish") { dependsOn(publication) }
    return publication
}
