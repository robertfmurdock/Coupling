package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.CurrentPairAssignmentsQuery
import com.zegreatrob.coupling.sdk.schema.PairAssignmentListQuery
import com.zegreatrob.coupling.sdk.schema.PairAssignmentRecordListQuery
import com.zegreatrob.coupling.sdk.schema.PartyMedianSpinDurationQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SdkPairAssignmentDocumentTest {

    private val repositorySetup = asyncSetup.extend(
        sharedSetup = { _ ->
            val sdk = sdk()
            object {
                val sdk = sdk
                val party = stubPartyDetails()
            }.apply { sdk.fire(SavePartyCommand(party)) }
        },
        sharedTeardown = {
            it.sdk.fire(DeletePartyCommand(it.party.id))
        },
    )

    @Test
    fun afterSavingUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val originalDateTime = Clock.System.now().roundToMillis()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }
    }) {
        sdk.fire(SavePairAssignmentsCommand(party.id, pairAssignmentDocument))
    } exercise {
        sdk.fire(SavePairAssignmentsCommand(party.id, updatedDocument))
    } verifyWithWait {
        sdk().fire(GqlQuery(PairAssignmentListQuery(party.id)))
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
            .map { it.pairAssignmentDetails.toModel() }
            .assertIsEqualTo(listOf(updatedDocument))
    }

    private fun Instant.roundToMillis() = toEpochMilliseconds().let(Instant::fromEpochMilliseconds)

    @Test
    fun deleteWhenDocumentDoesNotExistWillNotExplode() = repositorySetup().exercise {
        runCatching { sdk.fire(DeletePairAssignmentsCommand(party.id, PairAssignmentDocumentId.new())) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun getCurrentPairAssignmentsOnlyReturnsTheNewest() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = Clock.System.now().roundToMillis().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = Clock.System.now().roundToMillis())
            val newest = stubPairAssignmentDoc().copy(date = Clock.System.now().roundToMillis().plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { sdk.fire(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        sdk().fire(GqlQuery(CurrentPairAssignmentsQuery(partyId)))
            ?.party
            ?.currentPairAssignmentDocument
    } verify { result ->
        result?.pairAssignmentDetails?.toModel()
            .assertIsEqualTo(newest)
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val document = stubPairAssignmentDoc()
        }
    }) {
        sdk.fire(SavePairAssignmentsCommand(partyId, document))
    } exercise {
        sdk.fire(DeletePairAssignmentsCommand(partyId, document.id))
    } verifyWithWait {
        sdk().fire(GqlQuery(PairAssignmentListQuery(partyId)))
            ?.party
            ?.pairAssignmentDocumentList
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = Clock.System.now().roundToMillis().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = Clock.System.now().roundToMillis())
            val newest = stubPairAssignmentDoc().copy(date = Clock.System.now().roundToMillis().plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { sdk.fire(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        sdk().fire(GqlQuery(PairAssignmentListQuery(partyId)))
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
    } verifyWithWait { result ->
        result
            .map { it.pairAssignmentDetails.toModel() }
            .assertIsEqualTo(
                listOf(newest, middle, oldest),
            )
    }

    @Test
    fun canQueryMedianSpinDuration() = repositorySetup.with({
        object {
            val nowish = Clock.System.now().roundToMillis()
            val sdk = it.sdk
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = nowish.minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = nowish)
            val newest = stubPairAssignmentDoc().copy(date = nowish.plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { sdk.fire(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        sdk().fire(GqlQuery(PartyMedianSpinDurationQuery(partyId)))
            ?.party
            ?.medianSpinDuration
    } verify { result ->
        result.assertIsEqualTo(3.days)
    }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = repositorySetup() exercise {
        sdk().fire(GqlQuery(PairAssignmentListQuery(party.id)))
            ?.party
            ?.pairAssignmentDocumentList
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherParty = stubPartyDetails()
            val otherSdk: ActionCannon<CouplingSdkDispatcher> = otherSdk
        }
    }) {
        otherSdk.fire(SavePartyCommand(otherParty))
        otherSdk.fire(SavePairAssignmentsCommand(otherParty.id, stubPairAssignmentDoc()))
    } exercise {
        sdk().fire(GqlQuery(PairAssignmentListQuery(PartyId("someoneElseParty"))))
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.fire(DeletePartyCommand(otherParty.id))
    }

    @Test
    fun savedWillIncludeModificationDateAndUsername() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val pairAssignmentDoc = stubPairAssignmentDoc()
        }
    }) {
        sdk.fire(SavePairAssignmentsCommand(partyId, pairAssignmentDoc))
    } exercise {
        sdk().fire(GqlQuery(PairAssignmentRecordListQuery(partyId)))
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsRecentDateTime()
            modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }

    private fun Instant.assertIsRecentDateTime() = (Clock.System.now() - this)
        .compareTo(2.seconds)
        .assertIsEqualTo(-1)
}
