package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

interface PlayerRepository : PlayerListGet,
    PlayerSave,
    PlayerDelete,
    PlayerListGetDeleted

interface PlayerEmailRepository : PlayerRepository, PlayerListGetByEmail

interface PlayerDelete {
    suspend fun deletePlayer(tribeId: PartyId, playerId: String): Boolean
}

interface PlayerSave {
    suspend fun save(tribeIdPlayer: PartyElement<Player>)
}

interface PlayerListGet {
    suspend fun getPlayers(tribeId: PartyId): List<PartyRecord<Player>>
}

interface PlayerListGetDeleted {
    suspend fun getDeleted(tribeId: PartyId): List<PartyRecord<Player>>
}

interface PlayerListGetByEmail {
    suspend fun getPlayerIdsByEmail(email: String): List<PartyElement<String>>
}
