package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {

    @Test
    fun willSendToRepository() = asyncSetup(object : ServerSavePairAssignmentDocumentCommandDispatcher {
        override val currentPartyId: PartyId get() = PartyId("party-239")
        override val liveInfoRepository: LiveInfoRepository get() = TODO("Not yet implemented")
        override suspend fun PartyId.loadConnections(): List<CouplingConnection> = emptyList()
        override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? = null

        val pairAssignmentDocument = currentPartyId.with(
            PairAssignmentDocument(PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(), pairs = emptyList()),
        )

        override val pairAssignmentDocumentRepository = SpyPairAssignmentDocumentRepository()
            .apply { whenever(pairAssignmentDocument, Unit) }
    }) exercise {
        perform(SavePairAssignmentDocumentCommand(pairAssignmentDocument.element))
    } verifySuccess { result ->
        result.assertIsEqualTo(pairAssignmentDocument)
        pairAssignmentDocumentRepository.spyReceivedValues.assertIsEqualTo(listOf(pairAssignmentDocument))
    }
}

class SpyPairAssignmentDocumentRepository :
    PairAssignmentDocumentSave,
    Spy<PartyElement<PairAssignmentDocument>, Unit> by SpyData() {
    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) =
        spyFunction(partyPairDocument)
}
