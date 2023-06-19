package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.memory.MemoryPartyRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.slack.SlackUpdateSpin
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import korlibs.time.DateTime
import korlibs.time.TimeProvider
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {

    @Test
    fun willSendToRepository() = asyncSetup(object : ServerSavePairAssignmentDocumentCommandDispatcher {
        val party = stubParty()
        override val liveInfoRepository: LiveInfoRepository get() = TODO("Not yet implemented")
        override suspend fun PartyId.loadConnections(): List<CouplingConnection> = emptyList()
        override val partyRepository = MemoryPartyRepository("", TimeProvider)
        override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? = null

        val pairAssignmentDocument = party.id.with(
            PairAssignmentDocument(PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(), pairs = emptyList()),
        )
        override val slackRepository = SlackUpdateSpin { _, _, _ -> }
        override val slackAccessRepository = SlackAccessGet { null }

        override val pairAssignmentDocumentRepository = SpyPairAssignmentDocumentRepository()
            .apply { whenever(pairAssignmentDocument, Unit) }
    }) {
        partyRepository.save(party)
    } exercise {
        perform(SavePairAssignmentsCommand(party.id, pairAssignmentDocument.element))
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        pairAssignmentDocumentRepository.spyReceivedValues
            .assertIsEqualTo(listOf(pairAssignmentDocument))
    }
}

class SpyPairAssignmentDocumentRepository :
    PairAssignmentDocumentSave,
    Spy<PartyElement<PairAssignmentDocument>, Unit> by SpyData() {
    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) =
        spyFunction(partyPairDocument)
}
