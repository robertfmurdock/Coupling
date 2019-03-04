package com.zegreatrob.coupling.server.entity.user

interface UserSaveSyntax {

    val userRepository: UserRepository

    suspend fun User.save() {
        userRepository.save(this)
    }

}