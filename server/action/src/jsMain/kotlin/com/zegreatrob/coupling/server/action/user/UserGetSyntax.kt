package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.repository.user.UserRepository

interface UserGetSyntax {
    val userRepository: UserRepository
    suspend fun loadUser() = userRepository.getUser()?.data
}
