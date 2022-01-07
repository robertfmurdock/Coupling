package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.server.Process
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration

external val process: dynamic

const val primaryAuthorizedUsername = "couplingtestuser@gmail.com"
val primaryTestPassword = Process.getEnv("COUPLING_PRIMARY_TEST_PASSWORD") ?: ""

val primaryAuthorizedSdkDeferred by lazy {
    MainScope().async {
        authorizedKtorSdk(primaryAuthorizedUsername, primaryTestPassword)
    }
}

const val altAuthorizedUsername = "couplingtestuser.alt@gmail.com"
val altTestPassword = Process.getEnv("COUPLING_ALT_TEST_PASSWORD") ?: ""

val altAuthorizedSdkDeferred by lazy {
    MainScope().async {
        authorizedKtorSdk(altAuthorizedUsername, altTestPassword)
    }
}

private suspend fun authorizedKtorSdk(username: String, password: String) =
    AuthorizedKtorSdk(authorizedKtorClient(username, password))

suspend fun authorizedKtorSdk() = primaryAuthorizedSdkDeferred.await()

private val generalPurposeClient = HttpClient {
    install(JsonFeature)
    install(Logging) {
        KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
        val ktorLogger = KotlinLogging.logger("ktor")
        logger = object : Logger {
            override fun log(message: String) {
                ktorLogger.info { message }
            }
        }
        level = LogLevel.ALL
    }
}


private suspend fun authorizedKtorClient(username: String, password: String): HttpClient {
    val accessToken = generateAccessToken(username, password)

    val client = defaultClient().config {
        followRedirects = false
        val baseUrl = Url("${process.env.BASEURL}")

        defaultRequest {
            expectSuccess = false
            url {
                protocol = baseUrl.protocol
                host = baseUrl.host
                if (protocol != URLProtocol.HTTPS) {
                    port = baseUrl.port
                }
                encodedPath = "${baseUrl.encodedPath}$encodedPath"
            }
            header("Authorization", "Bearer $accessToken")
        }
        install(Logging) {
            val ktorLogger = KotlinLogging.logger("ktor")
            logger = object : Logger {
                override fun log(message: String) {
                    ktorLogger.info { message }
                }
            }
            level = LogLevel.ALL
        }
    }

    console.log("ktor logged in for $username")
    return client
}

private suspend fun generateAccessToken(username: String, password: String): String? {
    val result = generalPurposeClient.submitForm<JsonObject>(
        url = "https://zegreatrob.us.auth0.com/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg")
            append("client_secret", Process.getEnv("AUTH0_CLIENT_SECRET") ?: "")
            append("username", username)
            append("password", password)
        }
    )

    return result["id_token"]?.jsonPrimitive?.content
}

class AuthorizedKtorSdk(val client: HttpClient) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(object : KtorQueryPerformer {

        override val client get() = client

        override fun basename() = process.env.BASENAME.unsafeCast<String?>() ?: ""
    }) {
}