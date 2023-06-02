package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.repository.secret.SecretListGet
import com.zegreatrob.coupling.repository.secret.SecretSave
import korlibs.time.TimeProvider

class MemorySecretRepository(
    override val userId: String = "test-user",
    override val clock: TimeProvider = TimeProvider,
    private val recordBackend: RecordBackend<PartyElement<Secret>> = SimpleRecordBackend(),
) : SecretSave,
    SecretListGet,
    TypeRecordSyntax<PartyElement<Secret>>,
    RecordBackend<PartyElement<Secret>> by recordBackend {
    override suspend fun save(it: PartyElement<Secret>) = it.record().save()
    override suspend fun getSecrets(partyId: PartyId): List<PartyRecord<Secret>> = records
}
