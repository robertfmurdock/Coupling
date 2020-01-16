package com.zegreatrob.coupling.server.action.pairassignmentdocument

import SpyData
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPairAssignmentDoc
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThingsAsync() = testAsync {
        setupAsync(object : ProposeNewPairsCommandDispatcher, PinGet, TribeGet, PairAssignmentDocumentGet {
            override val actionDispatcher get() = throw NotImplementedError("Do not use")
            override val pairAssignmentDocumentRepository = this
            override val tribeRepository = this
            override val pinRepository = this

            val players = listOf(Player(name = "John"))
            val pins = listOf(Pin(name = "Bobby"))
            val history = listOf(stubPairAssignmentDoc())
            val tribe = Tribe(TribeId("Tribe Id! ${Random.nextInt(300)}"), PairingRule.PreferDifferentBadge)

            override suspend fun getPins(tribeId: TribeId) = pins.also { tribeId.assertIsEqualTo(tribe.id) }
            override suspend fun getPairAssignments(tribeId: TribeId) =
                history.also { tribeId.assertIsEqualTo(tribe.id) }

            override suspend fun getTribe(tribeId: TribeId) = tribe.also { tribeId.assertIsEqualTo(tribe.id) }

            val spy = SpyData<RunGameAction, PairAssignmentDocument>()
            override fun RunGameAction.perform() = spy.spyFunction(this)

            val expectedPairAssignmentDocument = stubPairAssignmentDoc()
        }) {
            spy.spyReturnValues.add(expectedPairAssignmentDocument)
        } exerciseAsync {
            ProposeNewPairsCommand(tribe.id, players)
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedPairAssignmentDocument)
            spy.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, tribe)))
        }
    }

}
