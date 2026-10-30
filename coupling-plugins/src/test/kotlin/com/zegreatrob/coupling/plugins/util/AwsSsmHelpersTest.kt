package com.zegreatrob.coupling.plugins.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AwsSsmHelpersTest {
    @Test
    fun `reads an individual secret by parameter name`() {
        assertEquals(
            "test-password",
            awsSsmParameterFrom(
                """{"Parameters":[{"Name":"/other","Value":"other"},{"Name":"/local/test-password","Value":"test-password"}]}""",
                "/local/test-password",
            ),
        )
    }

    @Test
    fun `rejects a missing secret without exposing other values`() {
        val failure = assertFailsWith<IllegalStateException> {
            awsSsmParameterFrom(
                """{"Parameters":[{"Name":"/other","Value":"private-value"}],"InvalidParameters":["/local/test-password"]}""",
                "/local/test-password",
            )
        }
        assertEquals("AWS SSM parameter is unavailable: /local/test-password.", failure.message)
    }

    @Test
    fun `rejects a blank secret`() {
        val failure = assertFailsWith<IllegalStateException> {
            awsSsmParameterFrom(
                """{"Parameters":[{"Name":"/local/test-password","Value":" "}]}""",
                "/local/test-password",
            )
        }
        assertEquals("AWS SSM parameter is unavailable: /local/test-password.", failure.message)
    }

    @Test
    fun `reads SSM values by parameter name`() {
        assertEquals(
            AwsParameters(
                serverlessAccessKey = "serverless-key",
                stripePublishableKey = "stripe-publishable-key",
                stripeSecretKey = "stripe-secret-key",
            ),
            awsParametersFrom(
                """
                {
                  "Parameters": [
                    {"Name": "/prerelease/stripe_sk", "Value": "stripe-secret-key"},
                    {"Name": "/local/SERVERLESS_ACCESS_KEY", "Value": "serverless-key"},
                    {"Name": "/prerelease/stripe_pk", "Value": "stripe-publishable-key"}
                  ]
                }
                """.trimIndent(),
            ),
        )
    }
}
