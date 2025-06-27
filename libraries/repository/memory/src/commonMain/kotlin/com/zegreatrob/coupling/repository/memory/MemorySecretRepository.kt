package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.SecretUsed
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.secret.SecretRepository
import kotlin.time.Clock

class MemorySecretRepository(
    override val userId: UserId = UserId.new(),
    override val clock: Clock = Clock.System,
    private val recordBackend: RecordBackend<PartyElement<Secret>> = SimpleRecordBackend(),
) : SecretRepository,
    TypeRecordSyntax<PartyElement<Secret>>,
    RecordBackend<PartyElement<Secret>> by recordBackend {
    override suspend fun save(it: PartyElement<Secret>) = it.record().save()
    override suspend fun save(used: SecretUsed) {
        TODO("Not yet implemented")
    }

    override suspend fun getSecrets(partyId: PartyId): List<PartyRecord<Secret>> = records
    override suspend fun deleteSecret(partyId: PartyId, secretId: SecretId): Boolean {
        TODO("Not yet implemented")
    }
}
