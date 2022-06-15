package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
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
            install(ContentNegotiation) {
                json()
            }
        }
        val audience = "https://localhost/api"
        val scope = "openid profile email offline_access"
        val clientId = "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg"
        val clientSecret = Process.getEnv("AUTH0_CLIENT_SECRET") ?: ""
        val result = client.submitForm(
            url = "https://zegreatrob.us.auth0.com/oauth/token",
            formParameters = Parameters.build {
                append("grant_type", "password")
                append("username", primaryAuthorizedUsername)
                append("password", primaryTestPassword)
                append("audience", audience)
                append("scope", scope)
                append("client_id", clientId)
                append("client_secret", clientSecret)
            }
        ).body<JsonObject>()
        val accessToken = result["access_token"]!!.jsonPrimitive.content
        val idToken = result["id_token"]!!.jsonPrimitive.content
        val expiresIn = result["expires_in"]!!.jsonPrimitive.int

        val key = "@@auth0spajs@@::$clientId::$audience::$scope"
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

        setAuth0CacheInLocalStorage(key, auth0Cache)
        WebdriverBrowser.setUrl("")
        PartyListPage.waitForPage()

        clearLogs()
    }

    private suspend fun setAuth0CacheInLocalStorage(key: String, auth0Cache: Json) {
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
    }
}

@JsModule("jwt-decode")
external fun jwtDecode(token: String): Json
