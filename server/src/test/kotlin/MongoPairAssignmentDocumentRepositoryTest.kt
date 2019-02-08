import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.internal.toDateTime
import kotlinx.coroutines.await
import kotlin.js.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull

private const val mongoUrl = "localhost/MongoPairAssignmentDocumentRepositoryTest"

class MongoPairAssignmentDocumentRepositoryTest {

    companion object : MongoPairAssignmentDocumentRepository, MonkToolkit {
        override val userContext = object : UserContext {
            override val username = "user-${Random.nextInt(200)}"
        }

        override val jsRepository: dynamic = jsRepository(mongoUrl)

        private val historyCollection: dynamic by lazy<dynamic> { getCollection("history", mongoUrl) }

        suspend fun dropHistory() {
            historyCollection.drop().unsafeCast<Promise<Unit>>().await()
        }

        suspend fun getDbHistory(tribeId: TribeId) =
                historyCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()

        private fun stubSimplePairAssignmentDocument(tribeId: TribeId, date: DateTime = DateTime.now()) =
                PairAssignmentDocumentId(id())
                        .let { id ->
                            id to stubPairAssignmentDoc(date, tribeId, id)
                        }

        private fun stubPairAssignmentDoc(
                date: DateTime = DateTime.now(),
                tribeId: TribeId,
                id: PairAssignmentDocumentId? = PairAssignmentDocumentId(id())
        ) =
                PairAssignmentDocument(
                        date = date,
                        pairs = listOf(
                                PinnedCouplingPair(listOf(Player(
                                        id = "zeId",
                                        badge = 1,
                                        email = "whoop whoop",
                                        name = "Johnny",
                                        imageURL = "publicDomain.png",
                                        callSignNoun = "Wily",
                                        callSignAdjective = "Rural Wolf"
                                ).withPins()))
                        ),
                        tribeId = tribeId.value,
                        id = id
                )
    }

    @Test
    fun givenSimplePairSaveAndGetWorks() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            val document = stubSimplePairAssignmentDocument(tribeId).second
        }) {
            dropHistory()
        } exerciseAsync {
            save(document)
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(document))
        }
    }

    @Test
    fun differentDocumentsBothShowUpInGetFromNewestToOldestByDate() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            val oldest = stubSimplePairAssignmentDocument(tribeId, date = DateTime.now().minus(3.days)).second
            val middle = stubSimplePairAssignmentDocument(tribeId, date = DateTime.now()).second
            val newest = stubSimplePairAssignmentDocument(tribeId, date = DateTime.now().plus(2.days)).second
        }) {
            dropHistory()
            listOf(middle, oldest, newest).forEach { save(it) }
        } exerciseAsync {
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(newest, middle, oldest))
        }
    }

    @Test
    fun willAssignIdWhenDocumentHasNone() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            val document = stubPairAssignmentDoc(id = null, tribeId = tribeId)
        }) {
            dropHistory()
        } exerciseAsync {
            save(document)
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            val resultId = result.getOrNull(0)?.id
            assertNotNull(resultId)
            result.assertIsEqualTo(listOf(document.copy(id = resultId)))
        }
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            private val pair = stubSimplePairAssignmentDocument(tribeId)
            val id = pair.first
            val document = pair.second
        }) {
            dropHistory()
            save(document)
        } exerciseAsync {
            delete(id)
        } verifyAsync { result ->
            result.assertIsEqualTo(Unit)
            getPairAssignmentsAsync(tribeId).await()
                    .assertIsEqualTo(emptyList())
        }
    }

    class SavingTheSameDocumentTwice {

        private suspend fun setupSavedDocument() = setupAsync(object {
            val tribeId = TribeId("boo")
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubSimplePairAssignmentDocument(tribeId, date = originalDateTime).second
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }) {
            dropHistory()
            save(pairAssignmentDocument)
        }

        @Test
        fun willNotDeleteOriginalRecord() = testAsync {
            setupSavedDocument() exerciseAsync {
                save(updatedDocument)
                getDbHistory(tribeId)
            } verifyAsync { result ->
                result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date?>()?.toDateTime() }
                        .map { it["date"].unsafeCast<Date?>()?.toDateTime() }
                        .assertIsEqualTo(listOf(
                                updatedDateTime,
                                originalDateTime
                        ))
            }
        }

        @Test
        fun getWillOnlyReturnTheUpdatedDocument() = testAsync {
            setupSavedDocument() exerciseAsync {
                save(updatedDocument)
                getPairAssignmentsAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(updatedDocument))
            }
        }
    }
}