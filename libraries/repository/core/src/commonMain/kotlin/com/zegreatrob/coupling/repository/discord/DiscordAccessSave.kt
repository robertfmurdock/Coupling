package com.zegreatrob.coupling.repository.discord

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.party.PartyElement

fun interface DiscordAccessSave {
    suspend fun save(discordTeamAccess: PartyElement<DiscordTeamAccess>)
}

interface DiscordAccessRepository : DiscordAccessSave
