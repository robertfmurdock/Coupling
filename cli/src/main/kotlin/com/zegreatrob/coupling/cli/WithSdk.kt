package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.ActionCannon
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
            httpClient = defaultClient(environment.audienceHost()),
        )
        runBlocking {
            doWork(sdk)
        }
    }
}

private fun Auth0Environment.audienceHost() = "https://${URL(audience).host}"
