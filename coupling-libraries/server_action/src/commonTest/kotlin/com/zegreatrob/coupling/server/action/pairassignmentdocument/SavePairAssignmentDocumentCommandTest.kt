package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {

    @Test
    fun willSendToRepository() = asyncSetup(object : SavePairAssignmentDocumentCommandDispatcher {
        override val currentTribeId: TribeId get() = TribeId("tribe-239")
        override val liveInfoRepository: LiveInfoRepository get() = TODO("Not yet implemented")
        override suspend fun TribeId.loadConnections(): List<CouplingConnection> = emptyList()
        override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? = null

        val pairAssignmentDocument = currentTribeId.with(
            PairAssignmentDocument(PairAssignmentDocumentId("${uuid4()}"), date = DateTime.now(), pairs = emptyList())
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

class SpyPairAssignmentDocumentRepository : PairAssignmentDocumentSave,
    Spy<TribeIdPairAssignmentDocument, Unit> by SpyData() {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) =
        spyFunction(tribeIdPairAssignmentDocument)
}
