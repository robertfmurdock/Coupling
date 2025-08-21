package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.user.UserGetByEmail
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface UserConnectedUsersSyntax {
    val userRepository: UserGetByEmail

    suspend fun UserDetails.connectedUsers() = coroutineScope {
        connectedEmails.map { async { userRepository.getUsersWithEmail(it).firstOrNull()?.data } }
            .awaitAll()
            .filterNotNull()
    }
}
