package com.zegreatrob.coupling.server.action.pairassignmentdocument

import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {
    @Test
    fun willSendToRepository() = testAsync {
        setupAsync(object : SavePairAssignmentDocumentCommandDispatcher {
            override val traceId = null
            val pairAssignmentDocument = TribeId("tribe-239").with(
                PairAssignmentDocument(
                        id = null,
                        date = DateTime.now(),
                        pairs = emptyList()
                    )
            )

            override val pairAssignmentDocumentRepository = SpyPairAssignmentDocumentRepository()
                .apply { whenever(pairAssignmentDocument, Unit) }
        }) exerciseAsync {
            SavePairAssignmentDocumentCommand(pairAssignmentDocument)
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(pairAssignmentDocument)
            pairAssignmentDocumentRepository.spyReceivedValues.assertIsEqualTo(listOf(pairAssignmentDocument))
        }
    }
}

class SpyPairAssignmentDocumentRepository : PairAssignmentDocumentSave,
    Spy<TribeIdPairAssignmentDocument, Unit> by SpyData() {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) =
        spyFunction(tribeIdPairAssignmentDocument)
}
