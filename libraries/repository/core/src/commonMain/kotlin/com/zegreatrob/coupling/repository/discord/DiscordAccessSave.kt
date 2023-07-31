package com.zegreatrob.coupling.repository.discord

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

fun interface DiscordAccessSave {
    suspend fun save(partyDiscordAccess: PartyElement<DiscordTeamAccess>)
}

fun interface DiscordAccessGet {
    suspend fun get(partyId: PartyId): PartyRecord<DiscordTeamAccess>?
}

interface DiscordAccessRepository : DiscordAccessSave, DiscordAccessGet
