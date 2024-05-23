package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.user.UserRepository

interface UserSaveSyntax {

    val userRepository: UserRepository

    suspend fun UserDetails.save() {
        userRepository.save(this)
    }
}
