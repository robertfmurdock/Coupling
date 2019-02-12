package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserContextSyntax

object FindOrCreateUserAction

interface FindOrCreateUserActionDispatcher : UserContextSyntax{

    val userRepository: UserRepository

    suspend fun FindOrCreateUserAction.perform(): User {
        val user = userRepository.getUser()

        return if (user != null) {
            user
        } else {
            val newUser = User(email = userEmail(), authorizedTribeIds = emptyList())
            userRepository.save(newUser)
            newUser
        }
    }
}