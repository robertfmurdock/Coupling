package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail

interface UserPlayersSyntax : UserIdProvider {
    val playerRepository: PlayerListGetByEmail
    suspend fun UserDetails.getPlayers() = playerRepository.getPlayersByEmail(listOf(email) + connectedEmails)
}
