package com.zegreatrob.coupling.repository.user

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User

interface UserRepository : UserGet, UserGetByEmail, UserSave

interface UserSave {
    suspend fun save(user: User)
}

interface UserGet {
    suspend fun getUser(): Record<User>?
}

interface UserGetByEmail {
    suspend fun getUsersWithEmail(email: String): List<Record<User>>
}
