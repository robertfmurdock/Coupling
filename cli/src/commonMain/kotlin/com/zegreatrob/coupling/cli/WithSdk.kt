package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.ActionCannon
import io.ktor.client.plugins.HttpRequestRetry
import kotlin.uuid.Uuid

interface SdkProvider {
    suspend fun sdk(
        env: String,
        echo: (String) -> Unit,
    ): ActionCannon<CouplingSdkDispatcher>?
}

object DefaultSdkProvider : SdkProvider {
    override suspend fun sdk(
        env: String,
        echo: (String) -> Unit,
    ): ActionCannon<CouplingSdkDispatcher>? = loadSdk(env, echo)
}

fun cannonSdkProvider(cannon: ActionCannon<CouplingSdkDispatcher>?): SdkProvider = object : SdkProvider {
    override suspend fun sdk(
        env: String,
        echo: (String) -> Unit,
    ): ActionCannon<CouplingSdkDispatcher>? = cannon ?: loadSdk(env, echo)
}

suspend fun withSdk(
    env: String,
    echo: (String) -> Unit,
    sdkProvider: SdkProvider = DefaultSdkProvider,
    doWork: suspend (ActionCannon<CouplingSdkDispatcher>) -> Unit,
) = sdkProvider.sdk(env, echo)
    ?.let { doWork(it) }

suspend fun loadSdk(
    env: String,
    echo: (String) -> Unit,
): ActionCannon<CouplingSdkDispatcher>? {
    val environment = Auth0Environment.map[env]
    if (environment != null) {
        val accessToken = getAccessToken()
        if (accessToken == null) {
            echo("You are not currently logged in.")
        } else {
            return actionCannon(accessToken, environment)
        }
    } else {
        echo("Environment not found.")
    }
    return null
}

private fun actionCannon(
    accessToken: String,
    environment: Auth0Environment,
): ActionCannon<CouplingSdkDispatcher> {
    val sdk: ActionCannon<CouplingSdkDispatcher> = couplingSdk(
        getIdTokenFunc = { accessToken },
        httpClient = defaultClient(environment.audienceHost()).config {
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
        },
        pipe = LoggingActionPipe(Uuid.random()),
    )
    return sdk
}

private fun Auth0Environment.audienceHost() = "https://${getHost(audience)}"

expect fun getHost(url: String): String
