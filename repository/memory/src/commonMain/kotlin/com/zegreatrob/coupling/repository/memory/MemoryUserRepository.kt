package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository

class MemoryUserRepository(override val userId: String, override val clock: TimeProvider) : UserRepository,
    TypeRecordSyntax<User>,
    RecordSaveSyntax<User> {

    override var records: List<Record<User>> = emptyList()

    override suspend fun save(user: User) = user.record().save()

    override suspend fun getUser() = records.lastOrNull { it.data.id == userId }

    override suspend fun getUsersWithEmail(email: String) = records.filter { it.data.email == email }
        .groupBy { it.data.id }
        .mapNotNull { group -> group.value.maxBy { it.timestamp } }

}
