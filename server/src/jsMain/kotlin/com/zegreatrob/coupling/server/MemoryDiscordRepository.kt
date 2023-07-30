package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository

class MemoryDiscordRepository : DiscordAccessRepository {
    override suspend fun save(discordTeamAccess: PartyElement<DiscordTeamAccess>) {
        TODO("Not yet implemented")
    }
}
