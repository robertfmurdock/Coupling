package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContextMint
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPairAssignmentDocumentRepositoryTest {

    private val repositorySetup = asyncTestTemplate<SdkPartyContext<SdkPairAssignmentsRepository>>(sharedSetup = {
        val sdk = authorizedSdk()
        val party = stubParty()
        sdk.partyRepository.save(party)
        SdkPartyContext(sdk, sdk.pairAssignmentDocumentRepository, party.id, MagicClock())
    }, sharedTeardown = {
        it.partyRepository.deleteIt(it.partyId)
    })

    @Test
    fun afterSavingUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument() = repositorySetup.with(
        object : PartyContextMint<SdkPairAssignmentsRepository>() {
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }.bind(),
    ) {
        repository.save(partyId.with(this.pairAssignmentDocument))
    } exercise {
        repository.save(partyId.with(this.updatedDocument))
    } verifyWithWait {
        this.repository.getPairAssignments(this.partyId).data()
            .map { it.document }
            .assertIsEqualTo(listOf(this.updatedDocument))
    }

    @Test
    fun deleteWhenDocumentDoesNotExistWillReturnFalse() = repositorySetup.with(
        object : PartyContextMint<SdkPairAssignmentsRepository>() {
            val id = PairAssignmentDocumentId("${uuid4()}")
        }.bind(),
    ) exercise {
        repository.deleteIt(partyId, this.id)
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun getCurrentPairAssignmentsOnlyReturnsTheNewest() = repositorySetup.with(
        object : PartyContextMint<SdkPairAssignmentsRepository>() {
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }.bind(),
    ) {
        partyId.with(listOf(this.middle, this.oldest, this.newest))
            .forEach { repository.save(it) }
    } exercise {
        repository.getCurrentPairAssignments(partyId)
    } verify { result: PartyRecord<PairAssignmentDocument>? ->
        result?.element.assertIsEqualTo(this.newest)
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = repositorySetup.with(
        object : PartyContextMint<SdkPairAssignmentsRepository>() {
            val document = stubPairAssignmentDoc()
        }.bind(),
    ) {
        repository.save(partyId.with(this.document))
    } exercise {
        repository.deleteIt(partyId, this.document.id)
    } verifyWithWait { result ->
        result.assertIsEqualTo(true)
        this.repository.getPairAssignments(this.partyId)
            .data()
            .map { it.document }
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() = repositorySetup.with(
        object : PartyContextMint<SdkPairAssignmentsRepository>() {
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }.bind(),
    ) {
        partyId.with(listOf(this.middle, this.oldest, this.newest))
            .forEach { repository.save(it) }
    } exercise {
        repository.getPairAssignments(partyId)
    } verifyWithWait { result ->
        result.data()
            .map { it.document }
            .assertIsEqualTo(
                listOf(
                    this.newest,
                    this.middle,
                    this.oldest,
                ),
            )
    }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = repositorySetup() exercise {
        repository.getPairAssignments(partyId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val sdk = authorizedSdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherParty = stubParty()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.partyRepository.save(otherParty)
        otherSdk.pairAssignmentDocumentRepository.save(otherParty.id.with(stubPairAssignmentDoc()))
    } exercise {
        sdk.pairAssignmentDocumentRepository.getPairAssignments(PartyId("someoneElseParty"))
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.partyRepository.deleteIt(otherParty.id)
    }

    @Test
    fun savedWillIncludeModificationDateAndUsername() =
        repositorySetup.with(
            object : PartyContextMint<SdkPairAssignmentsRepository>() {
                val pairAssignmentDoc = stubPairAssignmentDoc()
            }.bind(),
        ) {
            repository.save(partyId.with(pairAssignmentDoc))
        } exercise {
            repository.getPairAssignments(partyId)
        } verify { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                timestamp.assertIsRecentDateTime()
                modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            }
        }

    private fun DateTime.assertIsRecentDateTime() = (DateTime.now() - this)
        .compareTo(2.seconds)
        .assertIsEqualTo(-1)
}
