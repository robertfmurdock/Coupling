package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.User
import com.zegreatrob.coupling.model.user.UserRepository

interface UserSaveSyntax {

    val userRepository: UserRepository

    suspend fun User.save() {
        userRepository.save(this)
    }

}