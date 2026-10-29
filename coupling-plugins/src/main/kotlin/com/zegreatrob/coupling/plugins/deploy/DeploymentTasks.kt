package com.zegreatrob.coupling.plugins.deploy

import java.io.File
import javax.inject.Inject
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

abstract class ServerlessConfigTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val templateFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val packageJsonFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun render() {
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            serverlessConfigContents(
                templateFile.get().asFile.readText(),
                packageJsonFile.get().asFile.readText(),
            ),
        )
    }
}

abstract class S3SyncTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:Input
    abstract val destination: Property<String>

    @TaskAction
    fun sync() {
        execOperations.exec {
            commandLine(s3SyncCommand(sourceDirectory.get().asFile, destination.get()))
        }
    }
}

internal fun serverlessConfigContents(templateContents: String, packageJsonContents: String): String {
    val version = Regex("\\\"serverless\\\"\\s*:\\s*\\\"[^0-9]*(\\d+\\.\\d+\\.\\d+)\\\"")
        .find(packageJsonContents)
        ?.groupValues
        ?.get(1)
        ?: throw IllegalArgumentException("Unable to find the Serverless version in package manifest")
    val frameworkVersion = Regex("(?m)^frameworkVersion: '[^']*'$")

    require(frameworkVersion.containsMatchIn(templateContents)) {
        "Unable to find the frameworkVersion declaration in Serverless template"
    }
    return templateContents.replace(frameworkVersion, "frameworkVersion: '$version'")
}

internal fun s3SyncCommand(sourceDirectory: File, destination: String): List<String> = listOf(
    "aws",
    "s3",
    "sync",
    sourceDirectory.absolutePath,
    destination,
)
