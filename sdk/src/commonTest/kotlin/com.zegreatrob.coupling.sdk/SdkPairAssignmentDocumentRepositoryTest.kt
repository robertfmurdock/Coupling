package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<TribeContext<Sdk>>(sharedSetup = {
        val user = stubUser().copy(email = primaryAuthorizedUsername)
        val sdk = authorizedKtorSdk()
        val tribe = stubTribe()
        sdk.tribeRepository.save(tribe)
        TribeContextData(sdk, tribe.id, MagicClock(), user)
    }, sharedTeardown = {
        it.repository.tribeRepository.delete(it.tribeId)
    })

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup({
        val sdk = authorizedKtorSdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherTribe = stubTribe()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.tribeRepository.save(otherTribe)
        otherSdk.save(otherTribe.id.with(stubPairAssignmentDoc()))
    } exercise {
        sdk.getPairAssignments(TribeId("someoneElseTribe"))
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.tribeRepository.delete(otherTribe.id)
    }

    override fun savedWillIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<Sdk>() {
        val pairAssignmentDoc = stubPairAssignmentDoc()
    }.bind()) {
        repository.save(tribeId.with(pairAssignmentDoc))
    } exercise {
        repository.getPairAssignments(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsRecentDateTime()
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

    private fun DateTime.assertIsRecentDateTime() = (DateTime.now() - this)
        .compareTo(2.seconds)
        .assertIsEqualTo(-1)

}
