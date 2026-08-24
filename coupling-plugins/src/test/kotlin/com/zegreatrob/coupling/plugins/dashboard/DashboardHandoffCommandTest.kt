package com.zegreatrob.coupling.plugins.dashboard

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardHandoffCommandTest {
    @Test
    fun `substitutes runtime values in generated deploy command`() {
        val deployment = deploymentFile()

        assertEquals(
            listOf(
                "aws", "cloudformation", "deploy", "--stack-name", "ze-great-team-dashboard",
                "--template-file", "${deployment.parent}/template.yml",
                "--role-arn", "arn:aws:iam::123456789012:role/cloudformation",
                "--region", "us-east-1",
            ),
            dashboardHandoffCommand(
                deploymentFile = deployment,
                commandName = "deploy",
                runtimeValues = mapOf(
                    "AWS_REGION" to "us-east-1",
                    "STACK_NAME" to "ze-great-team-dashboard",
                    "AWS_CLOUDFORMATION_EXECUTION_ROLE_ARN" to "arn:aws:iam::123456789012:role/cloudformation",
                ),
                dryRun = false,
            ),
        )
    }

    @Test
    fun `dry run validates deployment template and skips upload`() {
        val deployment = deploymentFile()

        assertEquals(
            listOf(
                "aws",
                "cloudformation",
                "validate-template",
                "--template-body",
                "file://${deployment.parent}/template.yml",
                "--region",
                "us-east-1",
                "--no-cli-pager",
            ),
            dashboardHandoffCommand(
                deploymentFile = deployment,
                commandName = "deploy",
                runtimeValues = mapOf("AWS_REGION" to "us-east-1"),
                dryRun = true,
            ),
        )

        assertEquals(
            emptyList(),
            dashboardHandoffCommand(
                deploymentFile = deployment,
                commandName = "upload",
                runtimeValues = mapOf("AWS_REGION" to "us-east-1"),
                dryRun = true,
            ),
        )
    }

    @Test
    fun `reads the consumer gateway stack name from bootstrap manifest`() {
        assertEquals(
            "ze-great-team-dashboard-gateway",
            consumerGatewayStackName(
                bootstrapManifest(
                    """
                    {
                      "githubOidc": {
                        "consumerGatewayStackName": "ze-great-team-dashboard-gateway"
                      }
                    }
                    """.trimIndent(),
                ),
            ),
        )
    }

    private fun deploymentFile(): File {
        val releaseDirectory = createTempDirectory("dashboard-handoff").toFile()
        return releaseDirectory.resolve("deployment.json").apply {
            writeText(
                """
                {
                  "template": "template.yml",
                  "commands": {
                    "upload": ["aws", "s3", "cp", "$releaseDirectory/lambda.zip", "s3://bucket/key", "--region", "${'$'}AWS_REGION"],
                    "deploy": ["aws", "cloudformation", "deploy", "--stack-name", "${'$'}STACK_NAME", "--template-file", "$releaseDirectory/template.yml", "--role-arn", "${'$'}AWS_CLOUDFORMATION_EXECUTION_ROLE_ARN", "--region", "${'$'}AWS_REGION"]
                  }
                }
                """.trimIndent(),
            )
        }
    }

    private fun bootstrapManifest(contents: String): File = createTempDirectory("dashboard-bootstrap").toFile()
        .resolve("bootstrap.json")
        .apply { writeText(contents) }
}
