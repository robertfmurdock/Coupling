package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.common.entity.user.User
import com.zegreatrob.coupling.common.entity.user.UserRepository

object FindUserAction

interface FindUserActionDispatcher : UserEmailSyntax {

    val userRepository: UserRepository

    suspend fun FindUserAction.perform(): User? {
        return userRepository.getUser()
    }
}