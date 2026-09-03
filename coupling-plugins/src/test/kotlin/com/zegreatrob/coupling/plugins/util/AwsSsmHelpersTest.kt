package com.zegreatrob.coupling.plugins.util

import kotlin.test.Test
import kotlin.test.assertEquals

class AwsSsmHelpersTest {
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
