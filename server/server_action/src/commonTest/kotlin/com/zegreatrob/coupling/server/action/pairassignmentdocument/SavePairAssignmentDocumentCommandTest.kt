package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {
    @Test
    fun willSendToRepository() = asyncSetup(object : SavePairAssignmentDocumentCommandDispatcher {
        override val traceId = uuid4()
        val pairAssignmentDocument = TribeId("tribe-239").with(
            PairAssignmentDocument(id = null, date = DateTime.now(), pairs = emptyList())
        )

        override val pairAssignmentDocumentRepository = SpyPairAssignmentDocumentRepository()
            .apply { whenever(pairAssignmentDocument, Unit) }
    }) exercise {
        SavePairAssignmentDocumentCommand(pairAssignmentDocument)
            .perform()
    } verify { result ->
        result.assertIsEqualTo(pairAssignmentDocument)
        pairAssignmentDocumentRepository.spyReceivedValues.assertIsEqualTo(listOf(pairAssignmentDocument))
    }
}

class SpyPairAssignmentDocumentRepository : PairAssignmentDocumentSave,
    Spy<TribeIdPairAssignmentDocument, Unit> by SpyData() {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) =
        spyFunction(tribeIdPairAssignmentDocument)
}
