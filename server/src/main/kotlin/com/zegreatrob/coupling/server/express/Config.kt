package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.Process

object Config {
    val microsoft = MicrosoftConfig()
    val port get() = Process.getEnv("PORT")?.toIntOrNull() ?: 3000
    val disableLogging get() = Process.getEnv("DISABLE_LOGGING")?.toBoolean() ?: false
    const val secret = "maythefourthbewithyou"

    const val buildDate: String = "None"
    const val gitRev: String = "None"
    val googleClientID: String = Process.getEnv("GOOGLE_CLIENT_ID")
        ?: "24452716216-9lqe1p511qcf53kuihamdhggb05gbt4p.apps.googleusercontent.com"

    val AUTH0_CLIENT_ID get() = Process.getEnv("AUTH0_CLIENT_ID") ?: "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg"
    const val AUTH0_DOMAIN = "zegreatrob.us.auth0.com"
    val AUTH0_CLIENT_SECRET get() = Process.getEnv("AUTH0_CLIENT_SECRET") ?: "shh"
    val AUTH0_CALLBACK_URL get() = "${Process.getEnv("PUBLIC_URL") ?: "http://localhost:3000"}/auth/signin-auth0"

    val clientPath  = Process.getEnv("CLIENT_PATH") ?: "/no-client-path-found"
}
