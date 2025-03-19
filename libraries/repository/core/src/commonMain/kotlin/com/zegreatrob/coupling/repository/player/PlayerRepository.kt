package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import kotools.types.text.NotBlankString

interface PlayerRepository :
    PlayerSave,
    PlayerDelete,
    PlayerGetRepository

interface PlayerGetRepository :
    PlayerListGet,
    PlayerListGetDeleted

interface PlayerEmailRepository :
    PlayerRepository,
    PlayerListGetByEmail

interface PlayerDelete {
    suspend fun deletePlayer(partyId: PartyId, playerId: PlayerId): Boolean
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
    suspend fun getPlayerIdsByEmail(email: NotBlankString): List<PartyElement<PlayerId>>
}
