package com.zegreatrob.coupling.server.action.pairassignmentdocument

import SpyData
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThingsAsyncAndUseThemForRunGameAction() = testAsync {
        setupAsync(object : ProposeNewPairsCommandDispatcher {
            override val traceId = uuid4()
            override val actionDispatcher get() = throw NotImplementedError("Do not use")
            override val wheel: Wheel get() = throw NotImplementedError("Do not use")
            override val pairAssignmentDocumentRepository get() = stubRepository
            override val tribeRepository get() = stubRepository

            val stubRepository = object : TribeGet, PairAssignmentDocumentGet {
                override suspend fun getTribeRecord(tribeId: TribeId) = Record(tribe, modifyingUserId = "test")
                    .also { tribeId.assertIsEqualTo(tribe.id) }

                override suspend fun getPairAssignments(tribeId: TribeId): List<Record<TribeIdPairAssignmentDocument>> =
                    history
                        .map {
                            Record(tribe.id.with(it), modifyingUserId = "")
                        }
                        .also { tribeId.assertIsEqualTo(tribe.id) }
            }

            val players = listOf(Player(name = "John"))
            val pins = listOf(Pin(name = "Bobby"))
            val history = listOf(stubPairAssignmentDoc())
            val tribe = Tribe(TribeId("Tribe Id! ${Random.nextInt(300)}"), PairingRule.PreferDifferentBadge)
            val expectedPairAssignmentDocument = stubPairAssignmentDoc()

            val spy = SpyData<RunGameAction, PairAssignmentDocument>()
                .apply { spyReturnValues.add(expectedPairAssignmentDocument) }

            override fun RunGameAction.perform() = spy.spyFunction(this)

        }) exerciseAsync {
            ProposeNewPairsCommand(tribe.id, players, pins)
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedPairAssignmentDocument)
            spy.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, tribe)))
        }
    }

}
