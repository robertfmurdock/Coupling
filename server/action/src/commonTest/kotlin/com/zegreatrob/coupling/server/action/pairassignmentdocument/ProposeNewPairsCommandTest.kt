package com.zegreatrob.coupling.server.action.pairassignmentdocument

import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentGetter
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinGetter
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.TribeGet
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CompletableDeferred
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThingsAsync() = testAsync {
        setupAsync(object : ProposeNewPairsCommandDispatcher, PinGetter, TribeGet, PairAssignmentDocumentGetter {
            override val pairAssignmentDocumentRepository = this
            override val tribeRepository = this
            val players = listOf(Player(name = "John"))
            val pins = listOf(Pin(name = "Bobby"))
            val history = listOf(
                PairAssignmentDocument(
                    DateTime.now(),
                    emptyList(),
                    null
                )
            )
            val tribe = KtTribe(
                TribeId(
                    "Tribe Id! ${Random.nextInt(300)}"
                ), PairingRule.PreferDifferentBadge
            )

            override fun getPinsAsync(tribeId: TribeId) = CompletableDeferred(pins)
                .also { tribeId.assertIsEqualTo(tribe.id) }

            override fun getPairAssignmentsAsync(tribeId: TribeId) = CompletableDeferred(history)
                .also { tribeId.assertIsEqualTo(tribe.id) }

            override suspend fun getTribe(tribeId: TribeId): KtTribe? = tribe
                .also { tribeId.assertIsEqualTo(tribe.id) }

            override val pinRepository: PinGetter = this
            override val actionDispatcher = SpyRunGameActionDispatcher()

            val expectedPairAssignmentDocument =
                PairAssignmentDocument(
                    DateTime.now(),
                    listOf(),
                    null
                )

            init {
                actionDispatcher.spyReturnValues.add(expectedPairAssignmentDocument)
            }
        }) exerciseAsync {
            ProposeNewPairsCommand(tribe.id, players)
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedPairAssignmentDocument)
            actionDispatcher.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, tribe)))
        }
    }

}

class SpyRunGameActionDispatcher : RunGameActionDispatcher, Spy<RunGameAction, PairAssignmentDocument> by SpyData() {
    override val actionDispatcher: FindNewPairsActionDispatcher get() = cancel()

    override fun RunGameAction.perform(): PairAssignmentDocument = spyFunction(this)
}