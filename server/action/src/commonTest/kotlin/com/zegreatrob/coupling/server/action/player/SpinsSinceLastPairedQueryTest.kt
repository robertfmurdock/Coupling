package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SpinsSinceLastPairedQueryTest {

    @Test
    fun withTwoPlayersOnePairReportWillBeCreated() = asyncSetup(object : ServerSpinsSinceLastPairedQueryDispatcher {
        val players = CouplingPair.Double(stubPlayer(), stubPlayer())
        val partyId = stubPartyId()
        override val pairAssignmentDocumentRepository = PairAssignmentDocumentGet { emptyList() }
    }) exercise {
        perform(SpinsSinceLastPairedQuery(partyId, players))
    } verify { result ->
        result.assertIsEqualTo(null)
    }
}
