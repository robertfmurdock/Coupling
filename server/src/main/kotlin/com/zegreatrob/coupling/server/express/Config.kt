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
}
