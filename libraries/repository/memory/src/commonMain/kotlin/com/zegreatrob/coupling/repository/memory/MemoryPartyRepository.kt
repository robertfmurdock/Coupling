package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.party.PartyRepository
import kotlin.time.Clock

class MemoryPartyRepository(
    override val userId: UserId,
    override val clock: Clock = Clock.System,
    private val recordBackend: RecordBackend<PartyDetails> = SimpleRecordBackend(),
    private val integrationRecordBackend: RecordBackend<PartyElement<PartyIntegration>> = SimpleRecordBackend(),
) : PartyRepository,
    TypeRecordSyntax<PartyDetails>,
    RecordBackend<PartyDetails> by recordBackend {

    private val integrationHelper = object :
        TypeRecordSyntax<PartyElement<PartyIntegration>>,
        RecordBackend<PartyElement<PartyIntegration>> by integrationRecordBackend {
        override val userId = this@MemoryPartyRepository.userId
        override val clock = this@MemoryPartyRepository.clock
    }

    override suspend fun save(party: PartyDetails) = party.record().save()

    override suspend fun save(integration: PartyElement<PartyIntegration>) = with(integrationHelper) {
        integration.record().save()
    }

    override suspend fun getIntegration(partyId: PartyId): Record<PartyIntegration>? = integrationHelper.records
        .firstOrNull { it.data.partyId == partyId }
        ?.let { Record(it.data.element, it.modifyingUserId, it.isDeleted, it.timestamp) }

    override suspend fun getDetails(partyId: PartyId) = partyId.findParty()
        ?.takeUnless { it.isDeleted }

    override suspend fun loadParties() = recordList()
        .filterNot { it.isDeleted }

    private fun recordList() = records.groupBy { (party) -> party.id }
        .map { it.value.last() }

    override suspend fun deleteIt(partyId: PartyId) = partyId.findParty()?.data.deleteRecord()

    private fun PartyDetails?.deleteRecord() = if (this == null) {
        false
    } else {
        deletionRecord().save()
        true
    }

    private fun PartyId.findParty() = recordList()
        .firstOrNull { it.data.id == this }
}
