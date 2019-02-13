package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.common.entity.user.User
import com.zegreatrob.coupling.common.entity.user.UserRepository
import com.zegreatrob.coupling.common.entity.user.UserSaveSyntax

object FindOrCreateUserAction

interface FindOrCreateUserActionDispatcher : UserEmailSyntax, UserSaveSyntax, UserGetSyntax {

    suspend fun FindOrCreateUserAction.perform(): User {
        val user = loadUser()
        return if (user != null) {
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

