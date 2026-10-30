package com.zegreatrob.coupling.server.discord

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DiscordModeTest {
    @Test
    fun defaultsToLive() = DiscordMode.parse(null).assertIsEqualTo(DiscordMode.LIVE)

    @Test
    fun acceptsLive() = DiscordMode.parse("live").assertIsEqualTo(DiscordMode.LIVE)

    @Test
    fun acceptsSimulated() = DiscordMode.parse("simulated").assertIsEqualTo(DiscordMode.SIMULATED)

    @Test
    fun rejectsUnknownMode() {
        assertFailsWith<IllegalArgumentException> { DiscordMode.parse("simulation") }
    }
}
