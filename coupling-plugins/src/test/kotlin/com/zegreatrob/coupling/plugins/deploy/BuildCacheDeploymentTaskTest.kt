package com.zegreatrob.coupling.plugins.deploy

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildCacheDeploymentTaskTest {
    private val template = File("/tmp/template.yaml")

    @Test
    fun `builds a CloudFormation deploy command`() {
        assertEquals(
            listOf(
                "aws", "cloudformation", "deploy",
                "--stack-name", "coupling-gradle-build-cache",
                "--template-file", template.absolutePath,
                "--capabilities", "CAPABILITY_NAMED_IAM",
                "--parameter-overrides",
                "DeploymentRoleName=CouplingDeploy",
                "PullRequestRoleName=LocalDevelopment",
                "--region", "us-east-1",
                "--no-cli-pager",
            ),
            buildCacheDeploymentCommand(
                template = template,
                stackName = "coupling-gradle-build-cache",
                region = "us-east-1",
                deploymentRoleName = "CouplingDeploy",
                pullRequestRoleName = "LocalDevelopment",
                dryRun = false,
            ),
        )
    }

    @Test
    fun `builds a validation command for dry runs`() {
        assertEquals(
            listOf(
                "aws", "cloudformation", "validate-template",
                "--template-body", "file://${template.absolutePath}",
                "--region", "us-east-1",
                "--no-cli-pager",
            ),
            buildCacheDeploymentCommand(
                template = template,
                stackName = "coupling-gradle-build-cache",
                region = "us-east-1",
                deploymentRoleName = "CouplingDeploy",
                pullRequestRoleName = "LocalDevelopment",
                dryRun = true,
            ),
        )
    }
}
