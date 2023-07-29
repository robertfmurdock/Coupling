package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult

interface ServerGrantDiscordAccessCommandDispatcher : GrantDiscordAccessCommand.Dispatcher {

    val discordRepository: DiscordRepository

    override suspend fun perform(command: GrantDiscordAccessCommand): VoidResult =
        when (val result = discordRepository.exchangeForWebhook(command.code)) {
            is DiscordRepository.ExchangeResult.Error -> VoidResult.Rejected
            is DiscordRepository.ExchangeResult.Success ->
                VoidResult.Accepted
                    .also { println("Grant got result $result") }
        }
}
