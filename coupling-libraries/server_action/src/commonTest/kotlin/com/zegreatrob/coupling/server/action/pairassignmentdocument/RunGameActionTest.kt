package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class RunGameActionTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object : RunGameActionDispatcher {
        override val execute = stubActionExecutor(NextPlayerAction::class)
        override val wheel: Wheel get() = throw NotImplementedError("Stubbed")

        val expectedDate = DateTime.now()
        override fun currentDate() = expectedDate
        val party = Party(PartyId("1"), PairingRule.LongestTime)
        val players = emptyList<Player>()
        val pins = emptyList<Pin>()
        val history = emptyList<PairAssignmentDocument>()
        val expectedPairingAssignments = listOf(
            pairOf(Player()),
            pairOf(Player())
        )
        val spy = SpyData<FindNewPairsAction, List<CouplingPair>>().apply {
            spyReturnValues.add(expectedPairingAssignments)
        }

        override fun perform(action: FindNewPairsAction): List<CouplingPair> = spy.spyFunction(action)

    }) exercise {
        perform(RunGameAction(players, pins, history, party))
    } verify { result ->
        result.assertIsEqualTo(PairAssignmentDocument(
            id = result.id,
            date = expectedDate,
            pairs = expectedPairingAssignments.map {
                PinnedCouplingPair(it.asArray().map { player -> player.withPins() })
            }
        ))
    }
}
