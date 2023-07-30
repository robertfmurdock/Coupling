package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import kotlinx.datetime.Clock

@Suppress("UNUSED_PARAMETER")
class DynamoDiscordRepository(userId: String, clock: Clock) :
    DiscordAccessRepository {
    override suspend fun save(discordTeamAccess: PartyElement<DiscordTeamAccess>) {
        println("saving discordTeamAccess $discordTeamAccess")
    }
}
