package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository

class MemoryDiscordRepository : DiscordAccessRepository {
    override suspend fun save(partyDiscordAccess: PartyElement<DiscordTeamAccess>) {
        TODO("Not yet implemented")
    }

    override suspend fun get(partyId: PartyId): PartyRecord<DiscordTeamAccess> {
        TODO("Not yet implemented")
    }
}
