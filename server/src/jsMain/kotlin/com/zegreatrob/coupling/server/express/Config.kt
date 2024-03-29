package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.Process
import org.w3c.dom.url.URL

object Config {
    val prereleaseMode: Boolean = Process.getEnv("ENABLE_PRERELEASE_FEATURES")?.toBoolean() ?: false
    val port get() = Process.getEnv("PORT")?.toIntOrNull() ?: 3000
    val disableLogging get() = Process.getEnv("DISABLE_LOGGING")?.toBoolean() ?: false
    const val BUILD_DATE: String = "None"
    const val GIT_REV: String = "None"

    val clientBasename: String = Process.getEnv("CLIENT_BASENAME") ?: ""
    val appTitle: String = Process.getEnv("APP_TITLE") ?: ""
    val clientUrl: String = Process.getEnv("CLIENT_URL")?.ifEmpty { null } ?: "/no-client-url-found"
    val cliUrl: String = Process.getEnv("CLI_URL")?.ifEmpty { null } ?: "/no-cli-url-found"
    val publicUrl = Process.getEnv("PUBLIC_URL") ?: "http://localhost:3000"
    val secretSigningSecret =
        Process.getEnv("SECRET_SIGNING_SECRET") ?: throw Exception("Missing secret signing secret")
    val slackClientId = Process.getEnv("SLACK_CLIENT_ID") ?: ""
    val slackClientSecret = Process.getEnv("SLACK_CLIENT_SECRET") ?: ""
    val slackSigningSecret = Process.getEnv("SLACK_SIGNING_SECRET") ?: ""
    val discordClientId = Process.getEnv("DISCORD_CLIENT_ID") ?: ""
    val discordClientSecret = Process.getEnv("DISCORD_CLIENT_SECRET") ?: ""

    val stripeSecretKey = Process.getEnv("STRIPE_SECRET_KEY") ?: ""

    val websocketHost = Process.getEnv("WEBSOCKET_HOST") ?: "${URL(publicUrl).host}/api/websocket"
    val apiGatewayManagementApiHost =
        Process.getEnv("API_GATEWAY_MANAGEMENT_API_HOST") ?: "${URL(publicUrl).protocol}//$websocketHost"
    val AUTH0_CLIENT_ID
        get() = Process.getEnv("AUTH0_CLIENT_ID")
            ?.takeUnless(String::isBlank)
            ?: "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg"
    const val AUTH0_DOMAIN = "zegreatrob.us.auth0.com"
}
