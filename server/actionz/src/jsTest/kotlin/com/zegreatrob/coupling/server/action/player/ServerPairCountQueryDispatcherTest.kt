package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.notEmptyListOf
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Instant

class ServerPairCountQueryDispatcherTest {
    @Test
    fun willCorrectlyCountSolos() = asyncSetup(object : ServerPairCountQueryDispatcher {
        val partyId = stubPartyId()
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val player3 = stubPlayer()
        override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet {
            listOf(
                stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(player1).withPins())),
                stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(player2).withPins())),
                stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(player1, player3).withPins())),
                stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(player1).withPins())),
            ).map { PartyRecord(partyId.with(it), "-".toNotBlankString().getOrThrow(), false, Instant.DISTANT_PAST) }
        }
    }) exercise {
        perform(PairCountQuery(partyId, CouplingPair.Single(player1)))
    } verify { result ->
        result.assertIsEqualTo(2)
    }
}
