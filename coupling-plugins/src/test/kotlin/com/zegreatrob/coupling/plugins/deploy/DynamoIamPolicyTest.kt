package com.zegreatrob.coupling.plugins.deploy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamoIamPolicyTest {
    private val server = File("../server")

    @Test
    fun `scoped statements cannot grant unrestricted actions or resources`() {
        val statements = ObjectMapper().readTree(server.resolve("deploy/dynamodb-iam.json"))
            .path("role").path("statements")

        assertTrue(statements.isArray && !statements.isEmpty)
        statements.forEach { statement ->
            assertTrue(statement.path("Action").isArray && !statement.path("Action").isEmpty)
            statement.path("Action").forEach { action ->
                assertTrue(action.asText().startsWith("dynamodb:"))
                assertFalse(action.asText().contains('*'))
            }
            assertTrue(statement.path("Resource").isArray && !statement.path("Resource").isEmpty)
            statement.path("Resource").forEach { resource ->
                val substitution = resource.path("Fn::Sub")
                val arn = if (substitution.isArray) substitution[0].asText() else substitution.asText()
                assertTrue(arn.startsWith("arn:\${AWS::Partition}:dynamodb:\${AWS::Region}:\${AWS::AccountId}:table/"))
                assertFalse(arn.endsWith("table/*"))
            }
        }
    }

    @Test
    fun `rendered service replaces managed full access with a scoped policy`() {
        val config = ObjectMapper(YAMLFactory()).readTree(
            serverlessConfigContents(
                server.resolve("serverless.yml").readText(),
                File("../libraries/js-dependencies/package.json").readText(),
            ),
        )

        assertEquals(
            "${'$'}{file(${'$'}{env:SERVER_DIR, '.'}/deploy/dynamodb-iam.json)}",
            config.path("provider").path("iam").asText(),
        )
        val role = ObjectMapper().readTree(server.resolve("deploy/dynamodb-iam.json")).path("role")
        assertFalse(role.has("managedPolicies"))
        assertEquals(
            ObjectMapper().readTree("""{"Fn::Equals":["${'$'}{sls:stage}","prod"]}"""),
            config.path("resources").path("Conditions").path("UnprefixedTargetTables"),
        )
    }
}
