package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository

class MemoryUserRepository(override val userEmail: String, override val clock: TimeProvider) : UserRepository,
    TypeRecordSyntax<User>,
    RecordSaveSyntax<User> {

    override var records: List<Record<User>> = emptyList()

    override suspend fun save(user: User) = user.record().save()

    override suspend fun getUser() = records.lastOrNull { it.data.email == userEmail }

}
