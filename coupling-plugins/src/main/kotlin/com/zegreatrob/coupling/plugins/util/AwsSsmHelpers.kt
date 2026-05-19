package com.zegreatrob.coupling.plugins.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.provider.ProviderFactory

data class AwsParameters(
    val serverlessAccessKey: String,
    val stripePublishableKey: String,
    val stripeSecretKey: String,
)

fun ProviderFactory.fetchAwsSsmParameters(): AwsParameters {
    val (sak, pk, sk) = exec {
        commandLine(
            "/bin/bash",
            "-c",
            "aws ssm get-parameters --names /local/SERVERLESS_ACCESS_KEY /prerelease/stripe_pk /prerelease/stripe_sk --with-decryption | jq '[.Parameters[].Value']",
        )
    }.standardOutput.asText.get().toByteArray().let { ObjectMapper().readValue(it, List::class.java) }

    return AwsParameters(
        serverlessAccessKey = sak.toString(),
        stripePublishableKey = pk.toString(),
        stripeSecretKey = sk.toString(),
    )
}
