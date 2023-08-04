package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.datetime.Clock

class MemoryUserRepository(
    override val userId: String,
    override val clock: Clock,
    private val recordBackend: RecordBackend<UserDetails> = SimpleRecordBackend(),
) : UserRepository,
    TypeRecordSyntax<UserDetails>,
    RecordBackend<UserDetails> by recordBackend {
    override suspend fun save(user: UserDetails) = user.record().save()
    override suspend fun getUser() = records.lastOrNull { it.data.id == userId }
    override suspend fun getUsersWithEmail(email: String) = records.filter { it.data.email == email }
        .groupBy { it.data.id }
        .mapNotNull { group -> group.value.maxByOrNull { it.timestamp } }
}
