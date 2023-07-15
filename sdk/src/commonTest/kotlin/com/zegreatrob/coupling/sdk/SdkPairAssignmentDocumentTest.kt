package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import korlibs.time.DateTime
import korlibs.time.days
import korlibs.time.seconds
import kotlin.test.Test

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
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }
    }) {
        sdk.fire(SavePairAssignmentsCommand(party.id, pairAssignmentDocument))
    } exercise {
        sdk.fire(SavePairAssignmentsCommand(party.id, updatedDocument))
    } verifyWithWait {
        sdk.fire(graphQuery { party(party.id) { pairAssignmentDocumentList() } })
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
            .data()
            .map { it.document }
            .assertIsEqualTo(listOf(this.updatedDocument))
    }

    @Test
    fun deleteWhenDocumentDoesNotExistWillNotExplode() = repositorySetup().exercise {
        runCatching { sdk.fire(DeletePairAssignmentsCommand(party.id, PairAssignmentDocumentId("${uuid4()}"))) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun getCurrentPairAssignmentsOnlyReturnsTheNewest() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { sdk.fire(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        sdk.fire(graphQuery { party(partyId) { currentPairAssignments() } })
            ?.party
            ?.currentPairAssignmentDocument
    } verify { result: PartyRecord<PairAssignmentDocument>? ->
        result?.element.assertIsEqualTo(newest)
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
        sdk.fire(graphQuery { party(partyId) { pairAssignmentDocumentList() } })
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
            .data()
            .map(PartyElement<PairAssignmentDocument>::document)
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() = repositorySetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { sdk.fire(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        sdk.fire(graphQuery { party(partyId) { pairAssignmentDocumentList() } })
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
    } verifyWithWait { result ->
        result.data()
            .map { it.document }
            .assertIsEqualTo(
                listOf(newest, middle, oldest),
            )
    }

    @Test
    fun whenNoHistoryGetWillReturnEmptyList() = repositorySetup() exercise {
        sdk.fire(graphQuery { party(party.id) { pairAssignmentDocumentList() } })
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val sdk = sdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherParty = stubPartyDetails()
            val sdk = sdk
            val otherSdk: ActionCannon<CouplingSdkDispatcher> = otherSdk
        }
    }) {
        otherSdk.fire(SavePartyCommand(otherParty))
        otherSdk.fire(SavePairAssignmentsCommand(otherParty.id, stubPairAssignmentDoc()))
    } exercise {
        sdk.fire(graphQuery { party(PartyId("someoneElseParty")) { pairAssignmentDocumentList() } })
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
        sdk.fire(graphQuery { party(partyId) { pairAssignmentDocumentList() } })
            ?.party
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
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
