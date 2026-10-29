package com.zegreatrob.coupling.plugins.deploy

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeploymentTaskCommandsTest {
    @Test
    fun `renders the Serverless framework version without changing other YAML`() {
        assertEquals(
            """
            service: coupling-server
            frameworkVersion: '4.12.3'
            provider:
              runtime: nodejs22.x
            """.trimIndent(),
            serverlessConfigContents(
                """
                service: coupling-server
                frameworkVersion: '4'
                provider:
                  runtime: nodejs22.x
                """.trimIndent(),
                """{"devDependencies":{"serverless":"4.12.3"}}""",
            ),
        )
    }

    @Test
    fun `rejects a package manifest without a Serverless version`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            serverlessConfigContents("frameworkVersion: '4'", "{}")
        }

        assertEquals("Unable to find the Serverless version in package manifest", failure.message)
    }

    @Test
    fun `rejects a Serverless template without a framework version declaration`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            serverlessConfigContents("service: coupling-server", """{"serverless":"4.12.3"}""")
        }

        assertEquals("Unable to find the frameworkVersion declaration in Serverless template", failure.message)
    }

    @Test
    fun `builds an S3 sync command`() {
        assertEquals(
            listOf(
                "aws",
                "s3",
                "sync",
                "/tmp/coupling-cli",
                "s3://assets.zegreatrob.com/coupling-cli/1.2.3",
            ),
            s3SyncCommand(File("/tmp/coupling-cli"), "s3://assets.zegreatrob.com/coupling-cli/1.2.3"),
        )
    }

    @Test
    fun `builds the client S3 sync command`() {
        assertEquals(
            listOf(
                "aws",
                "s3",
                "sync",
                "/tmp/coupling-client",
                "s3://assets.zegreatrob.com/coupling/1.2.3",
            ),
            s3SyncCommand(File("/tmp/coupling-client"), "s3://assets.zegreatrob.com/coupling/1.2.3"),
        )
    }
}
