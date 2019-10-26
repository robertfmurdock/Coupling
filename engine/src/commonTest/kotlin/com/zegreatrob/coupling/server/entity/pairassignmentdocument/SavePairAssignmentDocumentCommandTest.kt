package com.zegreatrob.coupling.server.entity.pairassignmentdocument
import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.with
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {
    @Test
    fun willSendToRepository() = testAsync {
        setupAsync(object : SavePairAssignmentDocumentCommandDispatcher {
            val pairAssignmentDocument = PairAssignmentDocument(
                    date = DateTime.now(),
                    pairs = emptyList(),
                    id = null
            ).with(TribeId("tribe-239"))

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

class SpyPairAssignmentDocumentRepository : PairAssignmentDocumentSaver,
        Spy<TribeIdPairAssignmentDocument, Unit> by SpyData() {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) = spyFunction(tribeIdPairAssignmentDocument)
}
