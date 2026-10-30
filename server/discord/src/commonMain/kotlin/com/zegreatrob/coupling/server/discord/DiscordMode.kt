package com.zegreatrob.coupling.server.discord

import io.ktor.client.HttpClient

enum class DiscordMode {
    LIVE,
    SIMULATED,
    ;

    fun httpClient(): HttpClient = when (this) {
        LIVE -> HttpClient()
        SIMULATED -> simulatedDiscordHttpClient()
    }

    companion object {
        fun parse(value: String?): DiscordMode = when (value) {
            null, "live" -> LIVE
            "simulated" -> SIMULATED
            else -> throw IllegalArgumentException("DISCORD_MODE must be live or simulated")
        }
    }
}
