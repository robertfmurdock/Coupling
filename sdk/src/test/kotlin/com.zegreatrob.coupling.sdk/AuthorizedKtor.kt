package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.logging.JsonFormatter
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
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
            .apply { deleteAnyDisplayedTribes() }
    }
}

const val altAuthorizedUsername = "couplingtestuser.alt@gmail.com"
val altTestPassword = Process.getEnv("COUPLING_ALT_TEST_PASSWORD") ?: ""

val altAuthorizedSdkDeferred by lazy {
    MainScope().async {
        authorizedKtorSdk(altAuthorizedUsername, altTestPassword)
            .apply { deleteAnyDisplayedTribes() }
    }
}

private suspend fun AuthorizedKtorSdk.deleteAnyDisplayedTribes() {
    getTribes().forEach {
        delete(it.data.id)
    }
}

private suspend fun authorizedKtorSdk(username: String, password: String) =
    AuthorizedKtorSdk(generateAccessToken(username, password))

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

private val baseUrl = Url("https://localhost/local/")
private const val baseName = "/local"

private val ktorLogger = KotlinLogging.logger("ktor")

private fun buildClientWithToken(): HttpClient {
    js("process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = '0'")

    val client = defaultClient().config {
        followRedirects = false
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
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = ktorLogger.info { message }
            }
            level = LogLevel.INFO
        }
    }
    return client
}

private suspend fun generateAccessToken(username: String, password: String): String {
    val result = generalPurposeClient.submitForm<JsonObject>(
        url = "https://zegreatrob.us.auth0.com/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg")
            append("client_secret", Process.getEnv("AUTH0_CLIENT_SECRET") ?: "")
            append("username", username)
            append("password", password)
            append("audience", "https://localhost/api")
            append("scope", "email")
        }
    )

    return result["access_token"]?.jsonPrimitive?.content ?: ""
}

class AuthorizedKtorSdk(val token: String) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(object : KtorQueryPerformer {
        override suspend fun getIdToken(): String = token
        override val client by lazy(::buildClientWithToken)

        override fun basename() = baseName
    })
