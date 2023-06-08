package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.random.Random
import kotlin.test.Test

class SpinActionTest {

    @Test
    fun willUseRepositoryToGetThingsAsyncAndUseThemForRunGameAction() = asyncSetup(object : ServerSpinActionDispatcher {
        override val execute = stubActionExecutor(NextPlayerAction::class)

        override val wheel: Wheel get() = throw NotImplementedError("Do not use")

        val players = listOf(Player(name = "John", avatarType = null))
        val pins = listOf(Pin(name = "Bobby"))
        val history = listOf(stubPairAssignmentDoc())
        val party = Party(PartyId("Party Id! ${Random.nextInt(300)}"), PairingRule.PreferDifferentBadge)
        val expectedPairAssignmentDocument = stubPairAssignmentDoc()

        val spy = SpyData<RunGameAction, PairAssignmentDocument>()
            .apply { spyReturnValues.add(expectedPairAssignmentDocument) }

        override fun perform(action: RunGameAction) = spy.spyFunction(action)
    }) exercise {
        perform(SpinAction(party, players, pins, history))
    } verify { result ->
        result.assertIsEqualTo(expectedPairAssignmentDocument)
        spy.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, party)))
    }
}
