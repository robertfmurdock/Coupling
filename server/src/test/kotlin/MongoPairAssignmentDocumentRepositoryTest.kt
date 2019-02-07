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
    }

    @Test
    fun givenSimplePairSaveAndGetWorks() = testAsync {
        setupAsync(object {
            val tribeId = "tribe-id-99"
            val document = PairAssignmentDocument(
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
                    id = id()
            )
        }) {
            dropPlayers()
        } exerciseAsync {
            save(document)
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(document))
        }
    }
}