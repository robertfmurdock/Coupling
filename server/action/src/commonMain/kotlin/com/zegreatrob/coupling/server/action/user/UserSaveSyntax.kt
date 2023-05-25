package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository

interface UserSaveSyntax {

    val userRepository: UserRepository

    suspend fun User.save() {
        userRepository.save(this)
    }
}
