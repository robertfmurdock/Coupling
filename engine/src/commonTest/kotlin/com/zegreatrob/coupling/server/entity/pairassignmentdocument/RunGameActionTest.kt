package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class RunGameActionTest {

    @Test
    fun willBuildAGameRunWithAllAvailablePlayersAndThenReturnTheResults() = setup(object : RunGameActionDispatcher {
        override val actionDispatcher = SpyFindNewPairsActionDispatcher()
        val expectedDate = DateTime.now()
        override fun currentDate() = expectedDate
        val tribe = KtTribe(
            TribeId("1"),
            PairingRule.LongestTime
        )
        val players = emptyList<Player>()
        val pins = emptyList<Pin>()
        val history = emptyList<PairAssignmentDocument>()
        val expectedPairingAssignments = listOf(
                CouplingPair.Single(Player()),
                CouplingPair.Single(Player())
        )

        init {
            actionDispatcher.spyReturnValues.add(expectedPairingAssignments)
        }
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

class SpyFindNewPairsActionDispatcher : FindNewPairsActionDispatcher, Spy<FindNewPairsAction, List<CouplingPair>> by SpyData() {
    override val actionDispatcher: NextPlayerActionDispatcher get() = cancel()
    override val wheel: Wheel get() = cancel()
    override fun FindNewPairsAction.perform(): List<CouplingPair> = spyFunction(this)
}
