package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.time.ExperimentalTime

@Suppress("unused")
@ExperimentalTime
class MemoryPairAssignmentDocumentRepositoryTest :
    PairAssignmentDocumentRepositoryValidator<MemoryPairAssignmentDocumentRepository> {

    override val repositorySetup =
        asyncTestTemplate<PartyContext<MemoryPairAssignmentDocumentRepository>>(sharedSetup = {
            val clock = MagicClock()
            val user = stubUser()
            PartyContextData(MemoryPairAssignmentDocumentRepository(user.email, clock), stubPartyId(), clock, user)
        })
}
