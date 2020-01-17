package com.zegreatrob.coupling.server.action.pairassignmentdocument

import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class RunGameActionTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object : RunGameActionDispatcher {
        override val actionDispatcher: NextPlayerActionDispatcher get() = throw NotImplementedError("Stubbed")
        override val wheel: Wheel get() = throw NotImplementedError("Stubbed")

        val expectedDate = DateTime.now()
        override fun currentDate() = expectedDate
        val tribe = Tribe(TribeId("1"), PairingRule.LongestTime)
        val players = emptyList<Player>()
        val pins = emptyList<Pin>()
        val history = emptyList<PairAssignmentDocument>()
        val expectedPairingAssignments = listOf(
            CouplingPair.Single(Player()),
            CouplingPair.Single(Player())
        )
        val spy = SpyData<FindNewPairsAction, List<CouplingPair>>().apply {
            spyReturnValues.add(expectedPairingAssignments)
        }

        override fun FindNewPairsAction.perform(): List<CouplingPair> = spy.spyFunction(this)

    }) exercise {
        RunGameAction(players, pins, history, tribe).perform()
    } verify { result ->
        result.assertIsEqualTo(PairAssignmentDocument(
            expectedDate,
            expectedPairingAssignments.map {
                PinnedCouplingPair(it.asArray().map { player -> player.withPins() })
            }
        ))
    }
}
