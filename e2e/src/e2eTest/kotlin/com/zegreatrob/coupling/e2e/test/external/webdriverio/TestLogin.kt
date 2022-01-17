package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.e2e.test.external.webdriverio.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.server.Process
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.await
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.math.floor

object TestLogin : BrowserSyntax {

    private suspend fun clearLogs() {
        WebdriverBrowser.getLogs()
    }

    suspend fun login() {
        val client = HttpClient {
            install(JsonFeature)
        }
        val audience = "https://localhost/api"
        val scope = "openid profile email"
        val clientId = "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg"
        val clientSecret = Process.getEnv("AUTH0_CLIENT_SECRET") ?: ""
        val result = client.submitForm<JsonObject>(url = "https://zegreatrob.us.auth0.com/oauth/token",
            formParameters = Parameters.build {
                append("grant_type", "password")
                append("username", primaryAuthorizedUsername)
                append("password", primaryTestPassword)
                append("audience", audience)
                append("scope", scope)
                append("client_id", clientId)
                append("client_secret", clientSecret)
            }
        )
        val accessToken = result["access_token"]!!.jsonPrimitive.content
        val idToken = result["id_token"]!!.jsonPrimitive.content
        val expiresIn = result["expires_in"]!!.jsonPrimitive.int

        val key = "@@auth0spajs@@::${clientId}::${audience}::${scope}"
        val auth0Cache = json(
            "body" to json(
                "client_id" to clientId,
                "access_token" to accessToken,
                "id_token" to idToken,
                "token_type" to "Bearer",
                "scope" to scope,
                "audience" to audience,
                "expires_in" to expiresIn,
                "decodedToken" to json(
                    "user" to jwtDecode(idToken),
                    "claims" to json("__raw" to idToken)
                ),
            ),
            "expiresAt" to (floor(Date.now() / 1000) + expiresIn)
        )

        @Suppress("UNUSED_VARIABLE") val storageStuff = JSON.stringify(
            json("key" to key, "auth0Cache" to auth0Cache)
        )

        js(
            """
            browser.executeAsync(function(nothing, done) {
                var result = JSON.parse(nothing)
                window.localStorage.setItem(result.key, JSON.stringify(result.auth0Cache));
                done()
                }, storageStuff);
"""
        ).unsafeCast<Promise<Unit>>()
            .await()
        WebdriverBrowser.setUrl("")
        TribeListPage.waitForPage()

        clearLogs()
    }
}

@JsModule("jwt-decode")
external fun jwtDecode(token: String): Json
