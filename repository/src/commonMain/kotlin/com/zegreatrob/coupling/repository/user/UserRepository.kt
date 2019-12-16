package com.zegreatrob.coupling.repository.user

import com.zegreatrob.coupling.model.user.User

interface UserRepository {
    suspend fun save(user: User)
    suspend fun getUser(): User?
}