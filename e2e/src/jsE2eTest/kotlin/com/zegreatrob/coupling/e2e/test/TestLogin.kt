package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.jwtDecode
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
            .forwardLogs()
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
                append("username", PRIMARY_AUTHORIZED_USERNAME)
                append("password", primaryTestPassword)
                append("audience", audience)
                append("scope", scope)
                append("client_id", clientId)
                append("client_secret", clientSecret)
            },
        ).body<JsonObject>()
        val accessToken = result["access_token"]!!.jsonPrimitive.content
        val idToken = result["id_token"]!!.jsonPrimitive.content
        val expiresIn = result["expires_in"]!!.jsonPrimitive.int

        val key = "@@auth0spajs@@::$clientId::$audience::$scope"
        val auth0Cache = json(
            "body" to json(
                "access_token" to accessToken,
                "audience" to audience,
                "client_id" to clientId,
                "expires_in" to expiresIn,
                "scope" to scope,
                "token_type" to "Bearer",
            ),
            "expiresAt" to (floor(Date.now() / 1000) + expiresIn),
        )
        val idCacheKey = "@@auth0spajs@@::$clientId::@@user@@"
        val auth0IdCache = json(
            "id_token" to idToken,
            "decodedToken" to json(
                "user" to jwtDecode(idToken),
                "claims" to json("__raw" to idToken),
            ),
        )

        setAuth0CacheInLocalStorage(key, auth0Cache, idCacheKey, auth0IdCache)
        WebdriverBrowser.setUrl("")
        PartyListPage.waitForPage()

        clearLogs()
    }

    private suspend fun setAuth0CacheInLocalStorage(
        cacheKey: String,
        auth0Cache: Json,
        idCacheKey: String,
        auth0IdCache: Json,
    ) {
        @Suppress("UNUSED_VARIABLE")
        val storageStuff = JSON.stringify(
            json(
                "cacheKey" to cacheKey,
                "auth0Cache" to auth0Cache,
                "idCacheKey" to idCacheKey,
                "auth0IdCache" to auth0IdCache,
            ),
        )

        js(
            """
                browser.executeAsync(function(nothing, done) {
                    var result = JSON.parse(nothing)
                    window.localStorage.setItem(result.cacheKey, JSON.stringify(result.auth0Cache));
                    window.localStorage.setItem(result.idCacheKey, JSON.stringify(result.auth0IdCache));
                    done()
                    }, storageStuff);
    """,
        ).unsafeCast<Promise<Unit>>()
            .await()
    }
}
