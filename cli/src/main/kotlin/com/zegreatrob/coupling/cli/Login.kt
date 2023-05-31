package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.cli.auth0.KtorAuth0Client
import com.zegreatrob.coupling.cli.auth0.PollResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds

class Login : CliktCommand() {

    private val env by option().default("production")

    override fun run() {
        val environment = Auth0Environment.map[env]

        if (environment != null) {
            runBlocking {
                val client = KtorAuth0Client()
                val result = client.getDeviceCodeRequest(environment)

                echo("Your user code is: ${result.userCode}")
                echo("Please follow link to authenticate: ${result.verificationUriComplete}")

                val pollResult = client.pollForSuccess(environment, result.deviceCode, result.interval)

                configFile.parentFile.mkdirs()

                configFile.writeText(
                    text = buildJsonObject {
                        put("accessToken", pollResult?.accessToken)
                        put("refreshToken", pollResult?.refreshToken)
                    }.toString(),
                )

                echo("Login complete! $pollResult")
            }
        } else {
            echo("Could not find client_id for environment $env")
        }
    }

    private suspend fun KtorAuth0Client.pollForSuccess(
        environment: Auth0Environment,
        deviceCode: String,
        interval: Int,
    ): PollResult? {
        var pollResult: PollResult?
        while (true) {
            pollResult = checkForResult(environment, deviceCode)
            when (pollResult.error) {
                null, "expired_token", "access_denied" -> break
            }
            delay(interval.seconds)
        }
        return pollResult
    }
}
