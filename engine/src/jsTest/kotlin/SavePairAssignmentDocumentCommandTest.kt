import com.soywiz.klock.DateTime
import kotlin.test.Test

class SavePairAssignmentDocumentCommandTest {
    @Test
    fun willSendToRepository() = testAsync {
        setupAsync(object : SavePairAssignmentDocumentCommandDispatcher {

            val pairAssignmentDocument = PairAssignmentDocument(
                    date = DateTime.now(),
                    pairs = emptyList(),
                    tribeId = "tribe-293",
                    id = null
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

class SpyPairAssignmentDocumentRepository : PairAssignmentDocumentSaver,
        Spy<PairAssignmentDocument, Unit> by SpyData() {
    override suspend fun save(pairAssignmentDocument: PairAssignmentDocument) = spyFunction(pairAssignmentDocument)
}
