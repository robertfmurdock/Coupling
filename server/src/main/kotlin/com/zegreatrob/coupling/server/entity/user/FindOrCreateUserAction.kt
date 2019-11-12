package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserRepository

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

