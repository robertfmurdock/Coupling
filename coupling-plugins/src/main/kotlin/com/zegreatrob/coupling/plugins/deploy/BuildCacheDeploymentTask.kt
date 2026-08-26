package com.zegreatrob.coupling.plugins.deploy

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BuildCacheDeploymentTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val template: RegularFileProperty

    @get:Input
    abstract val stackName: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val deploymentRoleName: Property<String>

    @get:Input
    abstract val pullRequestRoleName: Property<String>

    @get:Input
    abstract val dryRun: Property<Boolean>

    @TaskAction
    fun deploy() {
        execOperations.exec {
            commandLine(
                buildCacheDeploymentCommand(
                    template = template.get().asFile,
                    stackName = stackName.get(),
                    region = region.get(),
                    deploymentRoleName = deploymentRoleName.get(),
                    pullRequestRoleName = pullRequestRoleName.get(),
                    dryRun = dryRun.get(),
                ),
            )
        }
    }
}

internal fun buildCacheDeploymentCommand(
    template: java.io.File,
    stackName: String,
    region: String,
    deploymentRoleName: String,
    pullRequestRoleName: String,
    dryRun: Boolean,
): List<String> = if (dryRun) {
    listOf(
        "aws", "cloudformation", "validate-template",
        "--template-body", "file://${template.absolutePath}",
        "--region", region,
        "--no-cli-pager",
    )
} else {
    listOf(
        "aws", "cloudformation", "deploy",
        "--stack-name", stackName,
        "--template-file", template.absolutePath,
        "--capabilities", "CAPABILITY_NAMED_IAM",
        "--parameter-overrides",
        "DeploymentRoleName=$deploymentRoleName",
        "PullRequestRoleName=$pullRequestRoleName",
        "--region", region,
        "--no-cli-pager",
    )
}
