package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.Game
import com.zegreatrob.coupling.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.call
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.time.Clock

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
        val receivedActions = mutableListOf<Any?>()
        override val cannon = StubCannon<ShufflePairsActionInner>(receivedActions)

        val expectedDate = Clock.System.now()
        override fun currentDate() = expectedDate
        val party = PartyDetails(PartyId("1"), PairingRule.LongestTime)
        val players = notEmptyListOf(stubPlayer())
        val pins = emptyList<Pin>()
        val history = emptyList<PairingSet>()
        val expectedPairingAssignments = notEmptyListOf(
            pairOf(stubPlayer()),
            pairOf(stubPlayer()),
        )
        val expectedPinnedPairs = expectedPairingAssignments.map {
            PinnedCouplingPair(it.toNotEmptyList().map { player -> player.withPins() })
        }
    }) {
        call(cannon::given, FindNewPairsAction(Game(players, history, party.pairingRule)))
            .thenReturn(expectedPairingAssignments)
        call(cannon::given, AssignPinsAction(expectedPairingAssignments, pins, history))
            .thenReturn(expectedPinnedPairs)
    } exercise {
        perform(ShufflePairsAction(party, players, pins, history))
    } verify { result ->
        result.assertIsEqualTo(
            PairingSet(
                id = result.id,
                date = expectedDate,
                pairs = expectedPinnedPairs,
            ),
        )
    }
}
