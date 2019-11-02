package com.zegreatrob.coupling.model.user

interface UserRepository {
    suspend fun save(user: User)
    suspend fun getUser(): User?
}