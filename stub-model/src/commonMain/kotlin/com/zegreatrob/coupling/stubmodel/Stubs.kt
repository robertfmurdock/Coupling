import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player

fun stubSimplePairAssignmentDocument(date: DateTime = DateTime.now()) =
    PairAssignmentDocumentId(uuid4().toString())
        .let { id ->
            id to stubPairAssignmentDoc(date, id)
        }

fun stubPairAssignmentDoc(
    date: DateTime = DateTime.now(),
    id: PairAssignmentDocumentId? = PairAssignmentDocumentId(
        uuid4().toString()
    )
) =
    PairAssignmentDocument(
        date = date,
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
        id = id
    )