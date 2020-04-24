package com.zegreatrob.coupling.server

import kotlin.js.json

private const val microsoftConsumerTenantId = "common"

object Config {
    val microsoft = MicrosoftConfig()
    val port get() = Process.getEnv("PORT")?.toIntOrNull() ?: 3000
    val disableLogging get() = Process.getEnv("DISABLE_LOGGING")?.toBoolean() ?: false

    const val secret = "maythefourthbewithyou"
    const val buildDate: String = "None"
    const val gitRev: String = "None"
    val googleClientID: String = Process.getEnv("GOOGLE_CLIENT_ID")
        ?: "24452716216-9lqe1p511qcf53kuihamdhggb05gbt4p.apps.googleusercontent.com"
    val googleClientSecret: String = Process.getEnv("GOOGLE_CLIENT_SECRET")
        ?: "ZVTj-iV5ZzW3-6so_1Q-bSPQ"
}

class MicrosoftConfig {
    val identityMetadata =
        "https://login.microsoftonline.com/${microsoftConsumerTenantId}/v2.0/.well-known/openid-configuration"
    val clientID = Process.getEnv("AZURE_AD_CLIENT_ID")
        ?: "127e78ae-6b6a-4213-a83f-a644e2d1bb84"
    val clientSecret = Process.getEnv("AZURE_AD_CLIENT_SECRET")
        ?: "bhyrMCFDF9485=aoiJD1*%{"
    val responseType = "code id_token"
    val responseMode = "form_post"
    val redirectUrl = "${Process.getEnv("PUBLIC_URL") ?: "http://localhost:3000"}/auth/signin-microsoft"
    val allowHttpForRedirectUrl = true
    val validateIssuer = false
    val issuer = "https://login.microsoftonline.com/${microsoftConsumerTenantId}/v2.0"
    val passReqToCallback = false
    val useCookieInsteadOfSession = true
    val cookieEncryptionKeys = arrayOf(
        json(
            "key" to "12345678901234567890123456789012",
            "iv" to "123456789012"
        ),
        json(
            "key" to "abcdefghijklmnopqrstuvwxyzabcdef",
            "iv" to "abcdefghijkl"
        )
    )
    val scope = arrayOf(
        "email",
        "profile"
    )
    val loggingLevel = "warn"
    val nonceLifetime = null
    val nonceMaxAmount = 5
    val clockSkew = null
    val isB2C: Boolean? = null
}
