package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.seconds

class Welcome : CliktCommand() {

    override fun run() {
        echo("Welcome to Coupling CLI.")
    }
}

fun main(args: Array<String>) = Welcome()
    .subcommands(Login())
    .main(args)

class Login : CliktCommand() {

    val env by option().default("production")

    override fun run() {
        val environment = Auth0Environment.map[env]

        if (environment != null) {
            runBlocking {
                val result = auth0HttpClient.submitForm(
                    url = "https://$AUTH0_DOMAIN/oauth/device/code",
                    formParameters = Parameters.build {
                        append("audience", environment.audience)
                        append("scope", "email, offline_access")
                        append("client_id", environment.clientId)
                    },
                ).body<JsonObject>()

                val link = result["verification_uri_complete"]?.jsonPrimitive?.content
                val userCode = result["user_code"]?.jsonPrimitive?.content
                val deviceCode = result["device_code"]?.jsonPrimitive?.content
                val interval = result["interval"]?.jsonPrimitive?.content?.toIntOrNull()

                if (link != null && deviceCode != null && interval != null) {
                    echo("Please follow link to authenticate: $link")
                    echo("Your user code is: $userCode")

                    var pollResult: PollResult?
                    while (true) {
                        pollResult = checkForResult(environment, deviceCode)
                        when (pollResult.error) {
                            null, "expired_token", "access_denied" -> break
                        }
                        delay(interval.seconds)
                    }
                    println("Done with $pollResult")
                } else {
                    echo("no link found in $result")
                }
            }
        } else {
            echo("Could not find client_id for environment $env")
        }
    }

    private suspend fun checkForResult(
        environment: Auth0Environment,
        deviceCode: String,
    ) = auth0HttpClient.submitForm(
        url = "https://$AUTH0_DOMAIN/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            append("client_id", environment.clientId)
            append("device_code", deviceCode)
        },
    ).body<PollResult>()
}

val auth0HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }
    Logging { this.level = LogLevel.ALL }
}

const val AUTH0_DOMAIN = "zegreatrob.us.auth0.com"

const val couplingCliClientId = "V4sQOGgllEvZC328pnLhbrfr7VJjbOUx"

data class Auth0Environment(val clientId: String, val audience: String) {
    companion object {
        val map = mapOf(
            "production" to Auth0Environment(
                couplingCliClientId,
                "https://coupling.zegreatrob.com/api",
            ),
        )
    }
}

@Serializable
data class PollResult(
    val error: String? = null,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("id_token")
    val idToken: String? = null,
    @SerialName("token_type")
    val token_type: String? = null,
    @SerialName("expires_in")
    val expiresIn: Int? = null,
)
