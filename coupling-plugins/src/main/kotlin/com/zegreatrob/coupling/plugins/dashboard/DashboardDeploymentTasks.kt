package com.zegreatrob.coupling.plugins.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.inject.Inject

private val mapper = ObjectMapper()

abstract class DashboardArtifactUploadTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val parametersFile: RegularFileProperty

    @get:InputFile
    abstract val releaseFile: RegularFileProperty

    @get:InputFile
    abstract val lambdaZip: RegularFileProperty

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val dryRun: Property<Boolean>

    @TaskAction
    fun upload() {
        if (dryRun.get()) return
        val bucket = parametersFile.parameter("LambdaArtifactBucket")
        val artifactKey = releaseFile.releaseValue("artifactKey")
        execOperations.exec {
            commandLine(
                "aws",
                "s3",
                "cp",
                lambdaZip.get().asFile.absolutePath,
                "s3://$bucket/$artifactKey",
                "--region",
                region.get(),
                "--no-cli-pager",
            )
        }
    }
}

abstract class DashboardCloudFormationDeployTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val parametersFile: RegularFileProperty

    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val stackName: Property<String>

    @get:Input
    abstract val dryRun: Property<Boolean>

    @TaskAction
    fun deploy() {
        val template = templateFile.get().asFile.absolutePath
        if (dryRun.get()) {
            execOperations.exec {
                commandLine("aws", "cloudformation", "validate-template", "--template-body", "file://$template", "--region", region.get(), "--no-cli-pager")
            }
            return
        }
        execOperations.exec {
            commandLine(
                "aws", "cloudformation", "deploy", "--stack-name", stackName.get(),
                "--template-file", template, "--region", region.get(),
                "--capabilities", "CAPABILITY_NAMED_IAM", "--parameter-overrides",
                "file://${parametersFile.get().asFile.absolutePath}", "--no-fail-on-empty-changeset", "--no-cli-pager",
            )
        }
    }
}

abstract class DashboardEndpointHealthTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val stackName: Property<String>

    @get:OutputFile
    abstract val endpointFile: RegularFileProperty

    @TaskAction
    fun check() {
        val url = stackOutput("ServerUrl").removeSuffix("/")
        listOf("$url/health", url).forEach(::assertSuccessful)
        endpointFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("$url\n")
        }
    }

    private fun stackOutput(key: String): String {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine(
                "aws", "cloudformation", "describe-stacks", "--stack-name", stackName.get(),
                "--region", region.get(), "--query", "Stacks[0].Outputs[?OutputKey=='$key'].OutputValue",
                "--output", "text", "--no-cli-pager",
            )
            standardOutput = output
        }
        return output.toString().trim().takeIf(String::isNotBlank)
            ?: throw GradleException("CloudFormation stack ${stackName.get()} has no $key output")
    }

    private fun assertSuccessful(url: String) {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI(url)).GET().build(),
            HttpResponse.BodyHandlers.discarding(),
        )
        if (response.statusCode() !in 200..299) {
            throw GradleException("Dashboard endpoint $url returned HTTP ${response.statusCode()}")
        }
    }
}

private fun RegularFileProperty.parameter(key: String): String {
    val parameters = mapper.readTree(get().asFile)
    return parameters
        .firstOrNull { it.path("ParameterKey").asText() == key }
        ?.path("ParameterValue")
        ?.asText()
        ?.takeIf(String::isNotBlank)
        ?: throw GradleException("${get().asFile} is missing a non-empty $key parameter")
}

private fun RegularFileProperty.releaseValue(key: String): String = mapper.readTree(get().asFile)
    .path(key)
    .asText()
    .takeIf(String::isNotBlank)
    ?: throw GradleException("${get().asFile} is missing a non-empty $key")
