package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.repository.discord.DiscordAccessSave
import com.zegreatrob.coupling.repository.discord.DiscordOAuthRepository

interface ServerGrantDiscordAccessCommandDispatcher :
    GrantDiscordAccessCommand.Dispatcher,
    CurrentUserProvider {

    val discordOAuthRepository: DiscordOAuthRepository

    val discordRepository: DiscordRepository
    val discordAccessRepository: DiscordAccessSave

    override suspend fun perform(command: GrantDiscordAccessCommand): VoidResult {
        if (!Regex("[a-f0-9]{64}").matches(command.state) || !Regex("[a-f0-9]{64}").matches(command.verifier)) return VoidResult.Rejected
        if (!discordOAuthRepository.consume(discordOAuthHash(command.state), currentUser.id, command.partyId, discordOAuthHash(command.verifier))) return VoidResult.Rejected
        return when (val result = discordRepository.exchangeForWebhook(command.code)) {
            is DiscordRepository.ExchangeResult.Error -> VoidResult.Rejected

            is DiscordRepository.ExchangeResult.Success ->
                discordAccessRepository.save(command.partyId.with(result.discordTeamAccess))
                    .let { VoidResult.Accepted }
        }
    }
}
