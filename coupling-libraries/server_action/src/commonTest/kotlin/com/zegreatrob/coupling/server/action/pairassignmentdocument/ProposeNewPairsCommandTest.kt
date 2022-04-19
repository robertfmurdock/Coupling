package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.party.PartyGet
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.random.Random
import kotlin.test.Test

class ProposeNewPairsCommandTest {

    @Test
    fun willUseRepositoryToGetThingsAsyncAndUseThemForRunGameAction() = asyncSetup(object :
            ServerProposeNewPairsCommandDispatcher {
            override val execute = stubActionExecutor(NextPlayerAction::class)

            override val wheel: Wheel get() = throw NotImplementedError("Do not use")
            override val pairAssignmentDocumentRepository get() = stubRepository
            override val partyRepository get() = stubRepository

            val stubRepository = object : PartyGet, PairAssignmentDocumentGet {
                override suspend fun getPartyRecord(partyId: PartyId) = Record(party, modifyingUserId = "test")
                    .also { partyId.assertIsEqualTo(party.id) }

                override suspend fun getPairAssignments(partyId: PartyId) = history.map {
                    Record(party.id.with(it), modifyingUserId = "")
                }.also { partyId.assertIsEqualTo(party.id) }
            }

            val players = listOf(Player(name = "John"))
            val pins = listOf(Pin(name = "Bobby"))
            val history = listOf(stubPairAssignmentDoc())
            val party = Party(PartyId("Party Id! ${Random.nextInt(300)}"), PairingRule.PreferDifferentBadge)
            override val currentPartyId = party.id
            val expectedPairAssignmentDocument = stubPairAssignmentDoc()

            val spy = SpyData<RunGameAction, PairAssignmentDocument>()
                .apply { spyReturnValues.add(expectedPairAssignmentDocument) }

            override fun perform(action: RunGameAction) = spy.spyFunction(action)
        }) exercise {
        perform(ProposeNewPairsCommand(players, pins))
    } verifySuccess { result ->
        result.assertIsEqualTo(expectedPairAssignmentDocument)
        spy.spyReceivedValues.assertIsEqualTo(listOf(RunGameAction(players, pins, history, party)))
    }
}
