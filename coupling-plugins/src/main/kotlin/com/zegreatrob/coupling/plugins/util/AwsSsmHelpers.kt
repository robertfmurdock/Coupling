package com.zegreatrob.coupling.plugins.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class AwsParameters(
    val serverlessAccessKey: String,
    val stripePublishableKey: String,
    val stripeSecretKey: String,
)

private val awsSsmParameterNames = listOf(
    "/local/SERVERLESS_ACCESS_KEY",
    "/prerelease/stripe_pk",
    "/prerelease/stripe_sk",
)

abstract class AwsSsmParametersValueSource : ValueSource<AwsParameters, ValueSourceParameters.None> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): AwsParameters {
        val output = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(
                "aws", "ssm", "get-parameters", "--names", *awsSsmParameterNames.toTypedArray(), "--with-decryption",
            )
            standardOutput = output
            isIgnoreExitValue = true
        }
        check(result.exitValue == 0) {
            "Unable to read local AWS SSM parameters. Run `aws sso login` and retry the Gradle command."
        }
        return awsParametersFrom(output.toString(Charsets.UTF_8))
    }
}

fun ProviderFactory.fetchAwsSsmParameters(): Provider<AwsParameters> =
    of<AwsParameters, ValueSourceParameters.None>(AwsSsmParametersValueSource::class.java) {}

internal fun awsParametersFrom(response: String): AwsParameters {
    val parameterValues = runCatching {
        ObjectMapper().readTree(response)
            .path("Parameters")
            .associate { parameter -> parameter.path("Name").asText() to parameter.path("Value").asText() }
    }.getOrElse { error ->
        throw IllegalStateException("AWS SSM returned an invalid response.", error)
    }

    val missingNames = awsSsmParameterNames.filter { name -> parameterValues[name].isNullOrBlank() }
    check(missingNames.isEmpty()) { "AWS SSM parameters are unavailable: ${missingNames.joinToString()}." }

    return AwsParameters(
        serverlessAccessKey = parameterValues.getValue("/local/SERVERLESS_ACCESS_KEY"),
        stripePublishableKey = parameterValues.getValue("/prerelease/stripe_pk"),
        stripeSecretKey = parameterValues.getValue("/prerelease/stripe_sk"),
    )
}
