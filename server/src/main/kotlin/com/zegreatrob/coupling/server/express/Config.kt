package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.Process
import org.w3c.dom.url.URL

object Config {
    val TEST_LOGIN_ENABLED = Process.getEnv("TEST_LOGIN_ENABLED").equals("true", ignoreCase = true)
    val port get() = Process.getEnv("PORT")?.toIntOrNull() ?: 3000
    val disableLogging get() = Process.getEnv("DISABLE_LOGGING")?.toBoolean() ?: false
    const val secret = "maythefourthbewithyou"

    const val buildDate: String = "None"
    const val gitRev: String = "None"

    val clientBasename: String = Process.getEnv("CLIENT_BASENAME") ?: ""
    val clientUrl: String = Process.getEnv("CLIENT_URL")?.ifEmpty { null } ?: "/no-client-url-found"
    val publicUrl = Process.getEnv("PUBLIC_URL") ?: "http://localhost:3000"
    val cookieDomain = Process.getEnv("COOKIE_DOMAIN")
    val websocketHost = Process.getEnv("WEBSOCKET_HOST") ?: "${URL(publicUrl).host}/api/websocket"
    val AUTH0_CLIENT_ID get() = Process.getEnv("AUTH0_CLIENT_ID") ?: "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg"
    const val AUTH0_DOMAIN = "zegreatrob.us.auth0.com"
    val AUTH0_CLIENT_SECRET = Process.getEnv("AUTH0_CLIENT_SECRET") ?: "sigh"
    val AUTH0_CALLBACK_URL get() = "${Process.getEnv("PUBLIC_URL") ?: "http://localhost:3000"}${clientBasename}/auth/signin-auth0"
}
