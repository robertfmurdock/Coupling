package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import kotlinx.datetime.Clock
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class ShufflePairsActionTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object :
        ShufflePairsAction.Dispatcher {
        override val execute = stubActionExecutor(NextPlayerAction::class)
        override val wheel: Wheel get() = throw NotImplementedError("Stubbed")

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
        val spy = SpyData<FindNewPairsAction, NotEmptyList<CouplingPair>>().apply {
            spyReturnValues.add(expectedPairingAssignments)
        }

        override fun perform(action: FindNewPairsAction): NotEmptyList<CouplingPair> = spy.spyFunction(action)
    }) exercise {
        perform(ShufflePairsAction(party, players, pins, history))
    } verify { result ->
        result.assertIsEqualTo(
            PairAssignmentDocument(
                id = result.id,
                date = expectedDate,
                pairs = expectedPairingAssignments.map {
                    PinnedCouplingPair(it.asArray().map { player -> player.withPins() })
                },
            ),
        )
    }
}
