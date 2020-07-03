package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.invoke
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThingsAsyncAndUseThemForRunGameAction() = asyncSetup(object :
        ProposeNewPairsCommandDispatcher {
        override val execute = stubActionExecutor(NextPlayerAction::class)

        override val wheel: Wheel get() = throw NotImplementedError("Do not use")
        override val pairAssignmentDocumentRepository get() = stubRepository
        override val tribeRepository get() = stubRepository

        val stubRepository = object : TribeGet, PairAssignmentDocumentGet {
            override suspend fun getTribeRecord(tribeId: TribeId) = Record(tribe, modifyingUserId = "test")
                .also { tribeId.assertIsEqualTo(tribe.id) }

            override suspend fun getPairAssignments(tribeId: TribeId) = history.map {
                Record(tribe.id.with(it), modifyingUserId = "")
            }.also { tribeId.assertIsEqualTo(tribe.id) }
        }

        val players = listOf(Player(name = "John"))
        val pins = listOf(Pin(name = "Bobby"))
        val history = listOf(stubPairAssignmentDoc())
        val tribe = Tribe(TribeId("Tribe Id! ${Random.nextInt(300)}"), PairingRule.PreferDifferentBadge)
        val expectedPairAssignmentDocument = stubPairAssignmentDoc()

        val spy = SpyData<RunGameAction, PairAssignmentDocument>()
            .apply { spyReturnValues.add(expectedPairAssignmentDocument) }

        override fun perform(action: RunGameAction) = spy.spyFunction(action)
    }) exercise {
        perform(ProposeNewPairsCommand(tribe.id, players, pins))
    } verifySuccess { result ->
        result.assertIsEqualTo(expectedPairAssignmentDocument)
        spy.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, tribe)))
    }

}
