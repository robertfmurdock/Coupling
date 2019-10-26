package com.zegreatrob.coupling.server.entity.pairassignmentdocument
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.withPins
import com.zegreatrob.coupling.core.entity.pin.Pin
import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PinAssignmentSyntax
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PinAssignmentSyntaxTest {

    @Test
    fun willDoTheObviousThingWhenThereIsOnlyOnePinAndOnePlayer() = setup(object : PinAssignmentSyntax {
        val pins = listOf(Pin(name = "Lucky"))
        val pete = Player(name = "Pete")
        val players = listOf(CouplingPair.Single(pete))
    }) exercise {
        players.assign(pins)
    } verify { result: List<PinnedCouplingPair> ->
        result.assertIsEqualTo(listOf(PinnedCouplingPair(listOf(pete.withPins(pins)))))
    }

    @Test
    fun willAssignNoPinsWhenThereAreNoPlayers() = setup(object : PinAssignmentSyntax {
        val pins = listOf(Pin(name = "Lucky"))
        val players = emptyList<CouplingPair>()
    }) exercise {
        players.assign(pins)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}
