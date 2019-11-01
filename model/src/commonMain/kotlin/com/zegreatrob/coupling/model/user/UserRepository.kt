package com.zegreatrob.coupling.model.user

import com.zegreatrob.coupling.model.User

interface UserRepository {
    suspend fun save(user: User)
    suspend fun getUser(): User?
}