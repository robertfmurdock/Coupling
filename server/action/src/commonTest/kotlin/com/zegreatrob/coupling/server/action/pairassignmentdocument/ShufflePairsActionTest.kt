package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class ShufflePairsActionTest {

    interface ShufflePairsActionInner :
        FindNewPairsAction.Dispatcher<ShufflePairsActionInner>,
        NextPlayerAction.Dispatcher<ShufflePairsActionInner>,
        CreatePairCandidateReportListAction.Dispatcher<ShufflePairsActionInner>,
        CreatePairCandidateReportAction.Dispatcher,
        AssignPinsAction.Dispatcher

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = asyncSetup(object :
        ScopeMint(),
        ShufflePairsAction.Dispatcher<ShufflePairsActionInner> {
        val resultChannel = Channel<Any>()
        val receivedActions = mutableListOf<Any?>()
        override val cannon = StubCannon<ShufflePairsActionInner>(receivedActions, resultChannel)

        val expectedDate = Clock.System.now()
        override fun currentDate() = expectedDate
        val party = PartyDetails(PartyId("1"), PairingRule.LongestTime)
        val players = notEmptyListOf(stubPlayer())
        val pins = emptyList<Pin>()
        val history = emptyList<PairAssignmentDocument>()
        val expectedPairingAssignments = notEmptyListOf(
            pairOf(Player(avatarType = null)),
            pairOf(Player(avatarType = null)),
        )
        val expectedPinnedPairs = expectedPairingAssignments.map {
            PinnedCouplingPair(it.toNotEmptyList().map { player -> player.withPins() })
        }
    }) {
        cannon.immediateReturn[FindNewPairsAction(Game(players, history, party.pairingRule))] =
            expectedPairingAssignments
        cannon.immediateReturn[AssignPinsAction(expectedPairingAssignments, pins, history)] = expectedPinnedPairs
    } exercise {
        perform(ShufflePairsAction(party, players, pins, history))
    } verify { result ->
        result.assertIsEqualTo(
            PairAssignmentDocument(
                id = result.id,
                date = expectedDate,
                pairs = expectedPinnedPairs,
            ),
        )
    }
}
