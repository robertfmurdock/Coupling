import com.soywiz.klock.DateTime
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoPairAssignmentDocumentRepositoryTest"

fun jsRepository(@Suppress("UNUSED_PARAMETER") url: String): dynamic {
    @Suppress("UNUSED_VARIABLE")
    val clazz = js("require('../../../../lib/CouplingDataService').default")
    return js("new clazz(url)")
}

class MongoPairAssignmentDocumentRepositoryTest {

    companion object : MongoPairAssignmentDocumentRepository, MonkToolkit {
        override val jsRepository: dynamic = jsRepository(mongoUrl)

        private val historyCollection: dynamic by lazy<dynamic> {
            @Suppress("UNUSED_VARIABLE")
            val monk = js("require(\"monk\")")
            val db = js("monk.default('$mongoUrl')")
            db.get("history")
        }

        suspend fun dropPlayers() {
            historyCollection.drop().unsafeCast<Promise<Unit>>().await()
        }

        private fun stubSimplePairAssignmentDocument(tribeId: String) =
                PairAssignmentDocumentId(id())
                        .let { id ->
                            id to PairAssignmentDocument(
                                    date = DateTime.now(),
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
                                    tribeId = tribeId,
                                    id = id
                            )
                        }
    }

    @Test
    fun givenSimplePairSaveAndGetWorks() = testAsync {
        setupAsync(object {
            val tribeId = "tribe-id-99"
            val document = stubSimplePairAssignmentDocument(tribeId).second
        }) {
            dropPlayers()
        } exerciseAsync {
            save(document)
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(document))
        }
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = testAsync {
        setupAsync(object {
            val tribeId = "tribe-id-99"
            private val pair = stubSimplePairAssignmentDocument(tribeId)
            val id = pair.first
            val document = pair.second
        }) {
            dropPlayers()
            save(document)
        } exerciseAsync {
            delete(id)
        } verifyAsync { result ->
            result.assertIsEqualTo(Unit)
            getPairAssignmentsAsync(tribeId).await()
                    .assertIsEqualTo(emptyList())
        }
    }
}