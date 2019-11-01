package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax

object FindOrCreateUserAction : Action

interface FindOrCreateUserActionDispatcher : ActionLoggingSyntax, UserEmailSyntax, UserSaveSyntax, UserGetSyntax {

    suspend fun FindOrCreateUserAction.perform(): User = logAsync {
        val user = loadUser()
        if (user != null) {
            user
        } else {
            val newUser = User(email = userEmail, authorizedTribeIds = emptySet())
            newUser.save()
            newUser
        }
    }
}

interface UserGetSyntax {
    val userRepository: UserRepository
    suspend fun loadUser() = userRepository.getUser()
}

