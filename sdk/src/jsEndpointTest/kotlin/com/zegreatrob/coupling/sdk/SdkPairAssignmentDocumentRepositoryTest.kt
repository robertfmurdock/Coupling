package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubTribe
import kotlin.test.Test

class SdkPairAssignmentDocumentRepositoryTest :
    PairAssignmentDocumentRepositoryValidator {

    override suspend fun withRepository(handler: suspend (PairAssignmentDocumentRepository, TribeId) -> Unit) {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        val tribe = stubTribe()
        sdk.save(tribe)
        handler(sdk, tribe.id)
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {}) exerciseAsync {
            sdk.getPairAssignments(TribeId("someoneElseTribe"))
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

}