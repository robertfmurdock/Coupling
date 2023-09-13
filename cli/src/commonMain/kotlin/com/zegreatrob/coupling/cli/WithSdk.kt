@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.cli

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.ActionCannon
import io.ktor.client.plugins.HttpRequestRetry
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

fun withSdk(
    env: String,
    echo: (String) -> Unit,
    doWork: suspend (ActionCannon<CouplingSdkDispatcher>) -> Unit,
) {
    val environment = Auth0Environment.map[env]
    if (environment == null) {
        echo("Environment not found.")
    } else {
        val accessToken = getAccessToken()
        if (accessToken == null) {
            echo("You are not currently logged in.")
            return
        }

        val sdk: ActionCannon<CouplingSdkDispatcher> = couplingSdk(
            getIdTokenFunc = { accessToken },
            httpClient = defaultClient(environment.audienceHost()).config {
                install(HttpRequestRetry) {
                    retryOnServerErrors(maxRetries = 5)
                    exponentialDelay()
                }
            },
            pipe = LoggingActionPipe(uuid4()),
        )
        cliScope.launch {
            doWork(sdk)
        }
    }
}

private fun Auth0Environment.audienceHost() = "https://${getHost(audience)}"

expect fun getHost(url: String): String
