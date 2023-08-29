package com.zegreatrob.coupling.cli

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.ActionCannon
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.runBlocking
import java.net.URL

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
                install(Logging) {
                    level = LogLevel.ALL
                }
            },
            pipe = LoggingActionPipe(uuid4()),
        )
        runBlocking {
            doWork(sdk)
        }
    }
}

private fun Auth0Environment.audienceHost() = "https://${URL(audience).host}"
