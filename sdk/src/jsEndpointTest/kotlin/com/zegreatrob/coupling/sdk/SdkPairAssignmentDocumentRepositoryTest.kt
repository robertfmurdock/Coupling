package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPairAssignmentDoc
import stubTribe
import stubUser
import kotlin.test.Test

@Suppress("unused")
val setJasmineTimeout = js("jasmine.DEFAULT_TIMEOUT_INTERVAL=10000")

class SdkPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator {

    override suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PairAssignmentDocumentRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser().copy(email = "eT-user-${uuid4()}")
        val sdk = authorizedSdk(username = user.email)
        val tribe = stubTribe()
        sdk.save(tribe)
        handler(sdk, tribe.id, user)
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

    override fun savedWillIncludeModificationDateAndUsername() = super.testRepository { repository, tribeId, user, _ ->
        setupAsync(object {
            val pairAssignmentDoc = stubPairAssignmentDoc()
        }) {
            repository.save(tribeId.with(pairAssignmentDoc))
        } exerciseAsync {
            repository.getPairAssignments(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                timestamp.assertIsRecentDateTime()
                modifyingUserEmail.assertIsEqualTo(user.email)
            }
        }
    }

    private fun DateTime.assertIsRecentDateTime() = (DateTime.now() - this)
        .compareTo(2.seconds)
        .assertIsEqualTo(-1)

}