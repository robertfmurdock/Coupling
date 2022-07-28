package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.coupling.repository.validation.PartyContextMint
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPairAssignmentDocumentRepositoryTest :
    PairAssignmentDocumentRepositoryValidator<SdkPairAssignmentsRepository> {

    override val repositorySetup = asyncTestTemplate<SdkPartyContext<SdkPairAssignmentsRepository>>(sharedSetup = {
        val sdk = authorizedSdk()
        val tribe = stubParty()
        sdk.partyRepository.save(tribe)
        SdkPartyContext(sdk, sdk.pairAssignmentDocumentRepository, tribe.id, MagicClock())
    }, sharedTeardown = {
            it.sdk.partyRepository.deleteIt(it.partyId)
        })

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val sdk = authorizedSdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherTribe = stubParty()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.partyRepository.save(otherTribe)
        otherSdk.pairAssignmentDocumentRepository.save(otherTribe.id.with(stubPairAssignmentDoc()))
    } exercise {
        sdk.pairAssignmentDocumentRepository.getPairAssignments(PartyId("someoneElseTribe"))
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.partyRepository.deleteIt(otherTribe.id)
    }

    override fun savedWillIncludeModificationDateAndUsername() =
        repositorySetup.with(
            object : PartyContextMint<SdkPairAssignmentsRepository>() {
                val pairAssignmentDoc = stubPairAssignmentDoc()
            }.bind()
        ) {
            repository.save(partyId.with(pairAssignmentDoc))
        } exercise {
            repository.getPairAssignments(partyId)
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
