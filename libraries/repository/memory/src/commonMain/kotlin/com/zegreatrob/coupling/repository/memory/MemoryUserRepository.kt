package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotools.types.text.NotBlankString
import kotlin.time.Clock

class MemoryUserBackend : RecordBackend<UserDetails> {
    override var records: List<Record<UserDetails>> = emptyList()
    internal val mutex = Mutex()
    internal val consumedSecrets = mutableSetOf<SecretId>()
}

class MemoryUserRepository(
    override val userId: UserId,
    override val clock: Clock,
    private val recordBackend: MemoryUserBackend = MemoryUserBackend(),
) : UserRepository,
    TypeRecordSyntax<UserDetails>,
    RecordBackend<UserDetails> by recordBackend {
    override suspend fun save(user: UserDetails) = user.record().save()
    override suspend fun getUser() = records.lastOrNull { it.data.id == userId }
    override suspend fun getUsersWithEmail(email: NotBlankString) = records.filter { it.data.email == email }
        .groupBy { it.data.id }
        .mapNotNull { group -> group.value.maxByOrNull { it.timestamp } }
    override suspend fun connectUser(user: UserDetails, target: UserDetails, secretId: SecretId): Boolean = recordBackend.mutex.withLock {
        if (user.id != userId || user.id == target.id || user.email == target.email || target.connectSecretId != secretId) return@withLock false
        if (!recordBackend.consumedSecrets.add(secretId)) return@withLock false
        user.copy(connectedEmails = user.connectedEmails + target.email).record().save()
        target.copy(connectedEmails = target.connectedEmails + user.email, connectSecretId = null).record().save()
        true
    }
}
