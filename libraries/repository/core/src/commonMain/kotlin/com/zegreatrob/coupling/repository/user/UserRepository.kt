package com.zegreatrob.coupling.repository.user

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.UserDetails

interface UserRepository : UserGet, UserGetByEmail, UserSave

interface UserSave {
    suspend fun save(user: UserDetails)
}

interface UserGet {
    suspend fun getUser(): Record<UserDetails>?
}

interface UserGetByEmail {
    suspend fun getUsersWithEmail(email: String): List<Record<UserDetails>>
}
