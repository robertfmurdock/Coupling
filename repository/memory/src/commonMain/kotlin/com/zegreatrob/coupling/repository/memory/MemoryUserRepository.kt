package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository

class MemoryUserRepository(private val currentUserEmail: String) : UserRepository {

    private var users = emptyMap<User, DateTime>()

    override suspend fun save(user: User) = (user to DateTime.now()).let(::addToUserMap)

    private fun addToUserMap(it: Pair<User, DateTime>) {
        users = users + it
    }

    override suspend fun getUser() = users.filterKeys { it.email == currentUserEmail }.keys.lastOrNull()

}