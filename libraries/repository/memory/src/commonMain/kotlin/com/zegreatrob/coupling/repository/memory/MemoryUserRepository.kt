package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.datetime.Clock

class MemoryUserRepository(
    override val userId: String,
    override val clock: Clock,
    private val recordBackend: RecordBackend<User> = SimpleRecordBackend(),
) : UserRepository,
    TypeRecordSyntax<User>,
    RecordBackend<User> by recordBackend {
    override suspend fun save(user: User) = user.record().save()
    override suspend fun getUser() = records.lastOrNull { it.data.id == userId }
    override suspend fun getUsersWithEmail(email: String) = records.filter { it.data.email == email }
        .groupBy { it.data.id }
        .mapNotNull { group -> group.value.maxByOrNull { it.timestamp } }
}
