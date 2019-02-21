
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.with
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.entity.pairassignmentdocument.PairAssignmentDocumentSaver
import com.zegreatrob.coupling.server.entity.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.entity.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
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
