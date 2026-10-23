package com.zegreatrob.coupling.plugins.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.coupling.plugins.js.nodeModulesDir
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

private val mapper = ObjectMapper()

abstract class DashboardApplicationParametersTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val bootstrapConfig: RegularFileProperty

    @get:OutputFile
    abstract val parametersFile: RegularFileProperty

    @get:Input
    abstract val credentialsParameterArn: Property<String>

    @get:Internal
    abstract val nodeExecPath: Property<String>

    @get:Internal
    abstract val nodeBinDir: DirectoryProperty

    @get:Internal
    abstract val projectNodeModulesDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val output = parametersFile.get().asFile
        execOperations.exec {
            environment(
                "NODE_PATH",
                projectNodeModulesDir.get().asFile.absolutePath,
            )
            environment(
                "PATH",
                "${nodeBinDir.get().asFile.absolutePath}${File.pathSeparator}${System.getenv("PATH")}",
            )
            commandLine(
                nodeExecPath.get(),
                projectNodeModulesDir.get().asFile.resolve(".bin/ze-great-dashboard-aws").absolutePath,
                "parameters",
                "--bootstrap-config",
                bootstrapConfig.get().asFile.absolutePath,
                "--output",
                output.absolutePath,
            )
        }

        @Suppress("UNCHECKED_CAST")
        val parameters = JsonSlurper().parse(output) as MutableList<MutableMap<String, Any?>>
        parameters.removeIf { it["ParameterKey"] == "SecretReference" }
        credentialsParameterArn.orNull
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { arn ->
                parameters += mutableMapOf(
                    "ParameterKey" to "SecretReference",
                    "ParameterValue" to arn,
                )
            }
        output.writeText("${JsonOutput.prettyPrint(JsonOutput.toJson(parameters))}\n")
    }
}

fun DashboardApplicationParametersTask.setup(project: Project) {
    val nodeJs = NodeJsRootPlugin.apply(project.rootProject)
    @Suppress("DEPRECATION")
    nodeBinDir.set(nodeJs.requireConfigured().nodeBinDir)
    @Suppress("DEPRECATION")
    nodeExecPath.set(nodeJs.requireConfigured().executable)
    projectNodeModulesDir.set(project.nodeModulesDir)
}

abstract class DashboardHandoffCommandTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val deploymentFile: RegularFileProperty

    @get:Input
    abstract val commandName: Property<String>

    @get:Input
    abstract val runtimeValues: MapProperty<String, String>

    @get:Input
    abstract val dryRun: Property<Boolean>

    @TaskAction
    fun execute() {
        val command = dashboardHandoffCommand(
            deploymentFile = deploymentFile.get().asFile,
            commandName = commandName.get(),
            runtimeValues = runtimeValues.get(),
            dryRun = dryRun.get(),
        )
        if (command.isEmpty()) return
        execOperations.exec {
            commandLine(command)
        }
    }
}

abstract class DashboardEndpointHealthTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val bootstrapManifest: RegularFileProperty

    @get:Input
    abstract val region: Property<String>

    @get:InputFile
    abstract val lambdaArtifact: RegularFileProperty

    @get:Input
    abstract val dryRun: Property<Boolean>

    @get:OutputFile
    abstract val endpointFile: RegularFileProperty

    @TaskAction
    fun check() {
        if (dryRun.get()) return
        val url = stackOutput().removeSuffix("/")
        val expectedCodeSha = lambdaArtifact.get().asFile.inputStream().use { input ->
            Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(input.readBytes()))
        }
        val functionName = dashboardFunctionName(bootstrapManifest.get().asFile)
        awaitDeployedRelease(functionName, expectedCodeSha, listOf("$url/health", url))
        endpointFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("$url\n")
        }
    }

    private fun stackOutput(): String {
        val stackName = consumerGatewayStackName(bootstrapManifest.get().asFile)
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine(
                "aws", "cloudformation", "describe-stacks", "--stack-name", stackName,
                "--region", region.get(), "--query", "Stacks[0].Outputs[?OutputKey=='ApiEndpoint'].OutputValue",
                "--output", "text", "--no-cli-pager",
            )
            standardOutput = output
        }
        return output.toString().trim().takeIf(String::isNotBlank)
            ?: throw GradleException("CloudFormation stack $stackName has no ApiEndpoint output")
    }

    private fun assertSuccessful(url: String) {
        val response = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build()
            .send(
                HttpRequest.newBuilder(URI(url)).timeout(java.time.Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.discarding(),
            )
        if (response.statusCode() !in 200..299) {
            throw GradleException("Dashboard endpoint $url returned HTTP ${response.statusCode()}")
        }
    }

    private fun awaitDeployedRelease(functionName: String, expectedCodeSha: String, urls: List<String>) {
        val deadline = System.nanoTime() + RELEASE_WAIT_NANOS
        var lastObservedCodeSha: String? = null
        var lastHealthFailure: Exception? = null
        while (System.nanoTime() < deadline) {
            val output = ByteArrayOutputStream()
            execOperations.exec {
                commandLine(
                    "aws", "lambda", "get-function-configuration", "--function-name", functionName,
                    "--region", region.get(), "--query", "CodeSha256", "--output", "text", "--no-cli-pager",
                )
                standardOutput = output
            }
            lastObservedCodeSha = output.toString().trim()
            if (lastObservedCodeSha == expectedCodeSha) {
                try {
                    urls.forEach(::assertSuccessful)
                    return
                } catch (failure: Exception) {
                    lastHealthFailure = failure
                }
            }
            Thread.sleep(RELEASE_POLL_INTERVAL_MILLIS)
        }
        val deployedCode = lastObservedCodeSha ?: "unavailable"
        val healthDetails = lastHealthFailure?.let { "; latest health failure: ${it.message}" }.orEmpty()
        throw GradleException(
            "Dashboard Lambda $functionName did not serve the packaged release before timeout " +
                "(expected CodeSha256 $expectedCodeSha, observed $deployedCode)$healthDetails",
        )
    }

    private companion object {
        const val RELEASE_WAIT_NANOS = 5L * 60 * 1_000_000_000
        const val RELEASE_POLL_INTERVAL_MILLIS = 5_000L
    }
}

private fun dashboardFunctionName(bootstrapManifest: java.io.File): String = mapper.readTree(bootstrapManifest)
    .path("core")
    .path("dashboardFunctionName")
    .asText()
    .takeIf(String::isNotBlank)
    ?: throw GradleException("$bootstrapManifest is missing core.dashboardFunctionName")

internal fun consumerGatewayStackName(bootstrapManifest: java.io.File): String = mapper.readTree(bootstrapManifest)
    .path("githubOidc")
    .path("consumerGatewayStackName")
    .asText()
    .takeIf(String::isNotBlank)
    ?: throw GradleException("$bootstrapManifest is missing githubOidc.consumerGatewayStackName")

internal fun dashboardHandoffCommand(
    deploymentFile: java.io.File,
    commandName: String,
    runtimeValues: Map<String, String>,
    dryRun: Boolean,
): List<String> {
    val handoff = mapper.readTree(deploymentFile)
    if (dryRun) return dryRunCommand(handoff, deploymentFile, commandName, runtimeValues)
    val command = handoff.path("commands").path(commandName)
        .takeIf { it.isArray }
        ?.map { it.asText() }
        ?: throw GradleException("$deploymentFile has no $commandName handoff command")
    return command.map { argument ->
        if (argument.startsWith("$")) {
            runtimeValues[argument.removePrefix("$")] ?: argument
        } else {
            argument
        }
    }
        .also { resolved ->
            resolved.firstOrNull { it.matches(Regex("\\$[A-Z][A-Z0-9_]*")) }
                ?.let { throw GradleException("$deploymentFile has no runtime value for $it") }
        }
}

private fun dryRunCommand(
    handoff: com.fasterxml.jackson.databind.JsonNode,
    deploymentFile: java.io.File,
    commandName: String,
    runtimeValues: Map<String, String>,
): List<String> {
    if (commandName == "upload") return emptyList()
    if (commandName != "deploy") throw GradleException("$deploymentFile cannot dry-run $commandName")
    val template = handoff.path("template").asText().takeIf(String::isNotBlank)
        ?: throw GradleException("$deploymentFile is missing template")
    val region = runtimeValues["AWS_REGION"]?.takeIf(String::isNotBlank)
        ?: throw GradleException("$deploymentFile has no runtime value for \$AWS_REGION")
    return listOf(
        "aws",
        "cloudformation",
        "validate-template",
        "--template-body",
        "file://${deploymentFile.parentFile.resolve(template)}",
        "--region",
        region,
        "--no-cli-pager",
    )
}
