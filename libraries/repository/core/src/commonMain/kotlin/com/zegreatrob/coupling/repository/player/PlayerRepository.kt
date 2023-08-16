package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player

interface PlayerRepository :
    PlayerListGet,
    PlayerSave,
    PlayerDelete,
    PlayerListGetDeleted

interface PlayerEmailRepository : PlayerRepository, PlayerListGetByEmail

interface PlayerDelete {
    suspend fun deletePlayer(partyId: PartyId, playerId: String): Boolean
}

interface PlayerSave {
    suspend fun save(partyPlayer: PartyElement<Player>)
}

fun interface PlayerListGet {
    suspend fun getPlayers(partyId: PartyId): List<PartyRecord<Player>>
}

interface PlayerListGetDeleted {
    suspend fun getDeleted(partyId: PartyId): List<PartyRecord<Player>>
}

interface PlayerListGetByEmail {
    suspend fun getPlayerIdsByEmail(email: String): List<PartyElement<String>>
}
