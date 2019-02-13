package com.zegreatrob.coupling.common.entity.user

interface UserRepository {
    suspend fun save(user: User)
    suspend fun getUser(): User?
}