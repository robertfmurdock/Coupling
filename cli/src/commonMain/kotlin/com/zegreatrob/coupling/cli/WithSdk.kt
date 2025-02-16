package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.ActionCannon
import io.ktor.client.plugins.HttpRequestRetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun withSdk(
    scope: CoroutineScope,
    env: String,
    echo: (String) -> Unit,
    cannon: ActionCannon<CouplingSdkDispatcher>? = null,
    doWork: suspend (ActionCannon<CouplingSdkDispatcher>) -> Unit,
) = (cannon ?: loadSdk(env, echo))
    ?.let { scope.launch { doWork(it) } }

private fun loadSdk(
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
