package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserContextSyntax

object FindUserAction

interface FindUserActionDispatcher : UserContextSyntax {

    val userRepository: UserRepository

    suspend fun FindUserAction.perform(): User? {
        return userRepository.getUser()
    }
}