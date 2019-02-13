package com.zegreatrob.coupling.common.entity.user

interface UserSaveSyntax {

    val userRepository: UserRepository

    suspend fun User.save() {
        userRepository.save(this)
    }

}