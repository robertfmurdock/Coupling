package com.zegreatrob.coupling.server

object Config {
    val port get() = Process.getEnv("PORT")?.toIntOrNull() ?: 3000
    val mongoUrl: String
        get() = Process.getEnv("MONGOHQ_URL_MONGOURL")
            ?: Process.getEnv("MONGOHQ_URL")
            ?: "mongodb://localhost/Coupling"
    val tempMongoUrl: String
        get() = "${tempHost}/CouplingTemp"
    private val tempHost
        get() = (Process.getEnv("MONGO_CONNECTION")
            ?: "localhost")

    val secret = "maythefourthbewithyou"
    val buildDate: String = "None"
    val gitRev: String = "None"
}
