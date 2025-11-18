package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.discord.DiscordAccessSave

interface ServerGrantDiscordAccessCommandDispatcher : GrantDiscordAccessCommand.Dispatcher {

    val discordRepository: DiscordRepository
    val discordAccessRepository: DiscordAccessSave

    override suspend fun perform(command: GrantDiscordAccessCommand): VoidResult = when (val result = discordRepository.exchangeForWebhook(command.code)) {
        is DiscordRepository.ExchangeResult.Error -> VoidResult.Rejected

        is DiscordRepository.ExchangeResult.Success ->
            discordAccessRepository.save(command.partyId.with(result.discordTeamAccess))
                .let { VoidResult.Accepted }
    }
}
