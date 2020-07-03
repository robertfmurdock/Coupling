package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
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
        val pairAssignmentDocument = TribeId("tribe-239").with(
            PairAssignmentDocument(id = null, date = DateTime.now(), pairs = emptyList())
        )

        override val pairAssignmentDocumentRepository = SpyPairAssignmentDocumentRepository()
            .apply { whenever(pairAssignmentDocument, Unit) }
    }) exercise {
        perform(SavePairAssignmentDocumentCommand(pairAssignmentDocument))
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
