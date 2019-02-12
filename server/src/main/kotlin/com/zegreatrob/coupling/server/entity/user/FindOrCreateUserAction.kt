package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.common.entity.user.User

object FindOrCreateUserAction

interface FindOrCreateUserActionDispatcher : UserEmailSyntax, UserSaveSyntax, UserGetSyntax {

    suspend fun FindOrCreateUserAction.perform(): User {
        val user = loadUser()
        return if (user != null) {
            user
        } else {
            val newUser = User(email = userEmail, authorizedTribeIds = emptyList())
            newUser.save()
            newUser
        }
    }
}

interface UserGetSyntax {
    val userRepository: UserRepository
    suspend fun loadUser() = userRepository.getUser()
}

interface UserSaveSyntax {

    val userRepository: UserRepository

    suspend fun User.save() {
        userRepository.save(this)
    }

}
