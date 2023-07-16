package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.repository.secret.SecretRepository
import kotlinx.datetime.Clock

class MemorySecretRepository(
    override val userId: String = "test-user",
    override val clock: Clock = Clock.System,
    private val recordBackend: RecordBackend<PartyElement<Secret>> = SimpleRecordBackend(),
) : SecretRepository,
    TypeRecordSyntax<PartyElement<Secret>>,
    RecordBackend<PartyElement<Secret>> by recordBackend {
    override suspend fun save(it: PartyElement<Secret>) = it.record().save()
    override suspend fun getSecrets(partyId: PartyId): List<PartyRecord<Secret>> = records
    override suspend fun deleteSecret(partyId: PartyId, secretId: String): Boolean {
        TODO("Not yet implemented")
    }
}
