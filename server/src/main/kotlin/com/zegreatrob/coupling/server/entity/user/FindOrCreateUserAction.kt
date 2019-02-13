package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.user.User
import com.zegreatrob.coupling.common.entity.user.UserRepository
import com.zegreatrob.coupling.common.entity.user.UserSaveSyntax

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

