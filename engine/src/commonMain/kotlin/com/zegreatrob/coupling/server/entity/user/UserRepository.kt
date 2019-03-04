package com.zegreatrob.coupling.server.entity.user

interface UserRepository {
    suspend fun save(user: User)
    suspend fun getUser(): User?
}