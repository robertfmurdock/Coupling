package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
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
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import korlibs.time.DateTime
import korlibs.time.days
import korlibs.time.seconds
import kotlin.test.Test

class SdkPairAssignmentDocumentTest {

    private val repositorySetup = asyncSetup.extend(
        sharedSetup = { _ ->
            val sdk = sdk()
            object : CouplingSdk by sdk {
                val party = stubParty()
            }.apply { perform(SavePartyCommand(party)) }
        },
        sharedTeardown = {
            it.perform(DeletePartyCommand(it.party.id))
        },
    )

    @Test
    fun afterSavingUpdatedDocumentGetWillOnlyReturnTheUpdatedDocument() = repositorySetup.with({
        object : CouplingSdk by it {
            val party = it.party
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubPairAssignmentDoc().copy(date = originalDateTime)
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }
    }) {
        perform(SavePairAssignmentsCommand(party.id, pairAssignmentDocument))
    } exercise {
        perform(SavePairAssignmentsCommand(party.id, updatedDocument))
    } verifyWithWait {
        perform(graphQuery { party(party.id) { pairAssignmentDocumentList() } })
            ?.partyData
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
            .data()
            .map { it.document }
            .assertIsEqualTo(listOf(this.updatedDocument))
    }

    @Test
    fun deleteWhenDocumentDoesNotExistWillNotExplode() = repositorySetup().exercise {
        runCatching { perform(DeletePairAssignmentsCommand(party.id, PairAssignmentDocumentId("${uuid4()}"))) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun getCurrentPairAssignmentsOnlyReturnsTheNewest() = repositorySetup.with({
        object : CouplingSdk by it {
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { perform(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        perform(graphQuery { party(partyId) { currentPairAssignments() } })
            ?.partyData
            ?.currentPairAssignmentDocument
    } verify { result: PartyRecord<PairAssignmentDocument>? ->
        result?.element.assertIsEqualTo(newest)
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = repositorySetup.with({
        object : CouplingSdk by it {
            val partyId = it.party.id
            val document = stubPairAssignmentDoc()
        }
    }) {
        perform(SavePairAssignmentsCommand(partyId, document))
    } exercise {
        perform(DeletePairAssignmentsCommand(partyId, document.id))
    } verifyWithWait {
        perform(graphQuery { party(partyId) { pairAssignmentDocumentList() } })
            ?.partyData
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
            .data()
            .map(PartyElement<PairAssignmentDocument>::document)
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedDocumentsNewestToOldest() = repositorySetup.with({
        object : CouplingSdk by it {
            val partyId = it.party.id
            val oldest = stubPairAssignmentDoc().copy(date = DateTime.now().minus(3.days))
            val middle = stubPairAssignmentDoc().copy(date = DateTime.now())
            val newest = stubPairAssignmentDoc().copy(date = DateTime.now().plus(2.days))
        }
    }) {
        listOf(middle, oldest, newest)
            .forEach { perform(SavePairAssignmentsCommand(partyId, it)) }
    } exercise {
        perform(graphQuery { party(partyId) { pairAssignmentDocumentList() } })
            ?.partyData
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
        perform(graphQuery { party(party.id) { pairAssignmentDocumentList() } })
            ?.partyData
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
            val otherParty = stubParty()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.perform(SavePartyCommand(otherParty))
        otherSdk.perform(SavePairAssignmentsCommand(otherParty.id, stubPairAssignmentDoc()))
    } exercise {
        sdk.perform(graphQuery { party(PartyId("someoneElseParty")) { pairAssignmentDocumentList() } })
            ?.partyData
            ?.pairAssignmentDocumentList
            .let { it ?: emptyList() }
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.perform(DeletePartyCommand(otherParty.id))
    }

    @Test
    fun savedWillIncludeModificationDateAndUsername() = repositorySetup.with({
        object : CouplingSdk by it {
            val partyId = it.party.id
            val pairAssignmentDoc = stubPairAssignmentDoc()
        }
    }) {
        perform(SavePairAssignmentsCommand(partyId, pairAssignmentDoc))
    } exercise {
        perform(graphQuery { party(partyId) { pairAssignmentDocumentList() } })
            ?.partyData
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
