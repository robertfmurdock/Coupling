import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

var tribeCounter = 1

fun stubTribe() = Tribe(
    id = TribeId(uuidString()),
    name = "Stub Tribe $tribeCounter",
    alternateBadgeName = "Badgely",
    badgesEnabled = tribeCounter % 2 == 0,
    callSignsEnabled = tribeCounter % 2 == 1,
    defaultBadgeName = "Badgerton",
    email = "stuby@stub.edu",
    pairingRule = PairingRule.LongestTime,
    animationEnabled = tribeCounter % 2 == 0,
    animationSpeed = tribeCounter
).also { tribeCounter++ }

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
fun stubPin() = Pin(uuidString(), "pin $pinCounter", "icon time", stubPinTarget()).also { pinCounter++ }

var pinTargetCounter = 1
fun stubPinTarget(): PinTarget {
    val index = pinTargetCounter % PinTarget.values().size
    return PinTarget.values()[index]
        .also { pinTargetCounter++ }
}

fun stubSimplePairAssignmentDocument(date: DateTime = DateTime.now()) = PairAssignmentDocumentId(uuidString())
    .let { id ->
        id to stubPairAssignmentDoc().copy(date = date, id = id)
    }

fun stubPairAssignmentDoc() = PairAssignmentDocument(
    date = DateTime.now(),
    pairs = listOf(
        PinnedCouplingPair(
            listOf(
                stubPlayer().withPins()
            ),
            listOf(
                stubPin()
            )
        )
    ),
    id = PairAssignmentDocumentId(uuidString())
)

private fun uuidString() = uuid4().toString()