package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.versionOption
import com.zegreatrob.coupling.auth0.management.KtorAuth0Client
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class CouplingCli : SuspendingCliktCommand() {

    init {
        versionOption(Versions.couplingVersion)
        context {
            readEnvvar = { key -> getEnv(key) }
        }
    }

    override suspend fun run() {
        val accessToken = getAccessToken()
        if (accessToken == null) {
            echo("You are not currently logged in. Some functions will not work.")
            echo("Run `coupling login` to log in.")
        } else {
            val expiration = accessToken.expiration()
            val refreshToken = getRefreshToken()
            val env = getEnv()
            val environment = Auth0Environment.map[env]
            if (env != null && environment != null && refreshToken != null && expiration != null &&
                expiration < Clock.System.now().plus(15.minutes)
            ) {
                refreshAccessToken(refreshToken, environment, env)
            }
        }
    }

    private fun String.expiration(): Instant? = decodeJwt(this)["exp"]
        ?.toLong()
        ?.let { Instant.fromEpochSeconds(it) }

    private suspend fun refreshAccessToken(
        refreshToken: String,
        environment: Auth0Environment,
        env: String,
    ) {
        val client = KtorAuth0Client()
        val result = client.refreshAccess(refreshToken, environment.audience, environment.clientId)
        if (result.error == null) {
            saveTokens(result, env)
        } else {
            echo(result.error, err = true)
        }
    }
}

expect fun decodeJwt(accessToken: String): Map<String, String>
