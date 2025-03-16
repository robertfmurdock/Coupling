package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator<MemoryPairAssignmentDocumentRepository> {

    override val repositorySetup =
        asyncTestTemplate<PartyContext<MemoryPairAssignmentDocumentRepository>>(sharedSetup = {
            val clock = MagicClock()
            val user = stubUserDetails()
            PartyContextData(MemoryPairAssignmentDocumentRepository(user.id, clock), stubPartyId(), clock, user)
        })
}
