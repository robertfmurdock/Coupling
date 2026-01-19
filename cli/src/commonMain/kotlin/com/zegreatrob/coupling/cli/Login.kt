package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.zegreatrob.coupling.auth0.management.AccessResult
import com.zegreatrob.coupling.auth0.management.KtorAuth0Client
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds

class Login : SuspendingCliktCommand() {

    private val env by option().default("production")
    private val refresh by option().flag()

    override suspend fun run() {
        val environment = Auth0Environment.map[env]

        val client = KtorAuth0Client()
        if (environment != null) {
            if (refresh) {
                val refreshToken = loadTokens()?.refreshToken()
                if (refreshToken == null) {
                    echo("No refresh token found. Please login again.")
                    return
                }
                val result = client.refreshAccess(refreshToken, environment.audience, environment.clientId)
                if (result.error == null) {
                    saveTokens(result, env)
                    echo("Login complete!")
                } else {
                    echo(result.error, err = true)
                }
                return
            }

            val result = client.getDeviceCodeRequest(environment.audience, environment.clientId)

            echo("Your user code is: ${result.userCode}")
            echo("Please follow link to authenticate: ${result.verificationUriComplete}")

            openBrowser(result.verificationUriComplete)

            val pollResult = client.pollForSuccess(environment, result.deviceCode, result.interval)

            saveTokens(pollResult, env)

            echo("Login complete!")
        } else {
            echo("Could not find client_id for environment $env")
        }
    }

    private suspend fun KtorAuth0Client.pollForSuccess(
        environment: Auth0Environment,
        deviceCode: String,
        interval: Int,
    ): AccessResult {
        var pollResult: AccessResult?
        while (true) {
            pollResult = checkForResult(environment.clientId, deviceCode)
            when (pollResult.error) {
                null, "expired_token", "access_denied" -> break
            }
            delay(interval.seconds)
        }
        return pollResult
    }
}

expect fun openBrowser(uri: String)

suspend fun saveTokens(pollResult: AccessResult, env: String) {
    writeSecureData(
        "tokens",
        text = buildJsonObject {
            put("accessToken", pollResult.accessToken)
            put("refreshToken", pollResult.refreshToken)
            put("env", env)
        }.toString(),
    )
}
