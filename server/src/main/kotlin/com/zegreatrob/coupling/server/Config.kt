package com.zegreatrob.coupling.server

object Config {
    val port get() = Process.getEnv("PORT")?.toIntOrNull() ?: 3000
    val secret = "maythefourthbewithyou"
    val buildDate: String = "None"
    val gitRev: String = "None"
}
