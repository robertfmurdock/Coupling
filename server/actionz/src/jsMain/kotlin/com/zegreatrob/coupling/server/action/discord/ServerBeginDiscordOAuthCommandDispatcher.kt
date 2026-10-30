package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.action.BeginDiscordOAuthCommand
import com.zegreatrob.coupling.action.DiscordOAuthState
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.repository.discord.DiscordOAuthRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

interface ServerBeginDiscordOAuthCommandDispatcher :
    BeginDiscordOAuthCommand.Dispatcher,
    CurrentUserProvider {
    val discordOAuthRepository: DiscordOAuthRepository

    override suspend fun perform(command: BeginDiscordOAuthCommand): DiscordOAuthState? {
        if (!Regex("[a-f0-9]{64}").matches(command.challenge)) return null
        val state = newDiscordOAuthState()
        val expiry = Clock.System.now() + 10.minutes
        discordOAuthRepository.savePending(discordOAuthHash(state), currentUser.id, command.partyId, command.challenge, expiry.toEpochMilliseconds())
        return DiscordOAuthState(state, expiry.toString())
    }
}
