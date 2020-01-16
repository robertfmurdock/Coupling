import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

var playerCounter = 1
fun stubPlayer() = Player(
    id = uuidString(),
    badge = 1,
    name = "Tim $playerCounter",
    callSignAdjective = "Spicy",
    callSignNoun = "Meatball",
    email = "tim@tim.meat",
    imageURL = "italian.jpg"
).also { playerCounter++ }


var pinCounter = 1
fun stubPin() = Pin(uuidString(), "pin $pinCounter", "icon time").also { pinCounter++ }

fun stubSimplePairAssignmentDocument(date: DateTime = DateTime.now()) = PairAssignmentDocumentId(uuidString())
    .let { id ->
        id to stubPairAssignmentDoc().copy(date = date, id = id)
    }

fun stubPairAssignmentDoc() = PairAssignmentDocument(
    date = DateTime.now(),
    pairs = listOf(
        PinnedCouplingPair(
            listOf(
                Player(
                    id = "zeId",
                    badge = 1,
                    email = "whoop whoop",
                    name = "Johnny",
                    imageURL = "publicDomain.png",
                    callSignNoun = "Wily",
                    callSignAdjective = "Rural Wolf"
                ).withPins()
            )
        )
    ),
    id = PairAssignmentDocumentId(uuidString())
)

private fun uuidString() = uuid4().toString()