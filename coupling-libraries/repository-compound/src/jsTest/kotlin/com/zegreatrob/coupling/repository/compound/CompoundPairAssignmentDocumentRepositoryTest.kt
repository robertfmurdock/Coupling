package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.repository.validation.TribeContextData
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate

import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CompoundPairAssignmentDocumentRepositoryTest :
    PairAssignmentDocumentRepositoryValidator<CompoundPairAssignmentDocumentRepository> {

    private val compoundRepositorySetup = asyncTestTemplate(sharedSetup = {
        object {
            val stubUser = stubUser()
            val clock = MagicClock()

            val repository1 = MemoryPairAssignmentDocumentRepository(stubUser.email, clock)
            val repository2 = MemoryPairAssignmentDocumentRepository(stubUser.email, clock)

            val compoundRepo = CompoundPairAssignmentDocumentRepository(repository1, repository2)

            val tribeId = stubPartyId()
            val pairAssignmentDocument = stubPairAssignmentDoc()
        }
    })

    override val repositorySetup =
        compoundRepositorySetup.extend<TribeContext<CompoundPairAssignmentDocumentRepository>>(
            sharedSetup = {
                TribeContextData(it.compoundRepo, it.tribeId, it.clock, it.stubUser)
            })

    @Test
    fun saveWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(tribeId.with(pairAssignmentDocument))
    } verify {
        repository2.getPairAssignments(tribeId).map { it.data.document }
            .find { it.id == pairAssignmentDocument.id }
            .assertIsEqualTo(pairAssignmentDocument)
    }

    @Test
    fun deleteWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(tribeId.with(pairAssignmentDocument))
        compoundRepo.delete(tribeId, pairAssignmentDocument.id)
    } verify {
        repository2.getPairAssignments(tribeId).map { it.data.document }
            .find { it.id == pairAssignmentDocument.id }
            .assertIsEqualTo(null)
    }
}