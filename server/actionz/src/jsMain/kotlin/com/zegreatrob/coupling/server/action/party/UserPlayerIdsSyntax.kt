package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import kotools.types.text.NotBlankString

interface UserPlayerIdsSyntax : UserIdProvider {
    val playerRepository: PlayerListGetByEmail
    suspend fun getUserPlayerIds(email: NotBlankString) = playerRepository.getPlayerIdsByEmail(email)
}
