package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairCountQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        suspend fun perform(query: PairCountQuery): Int
    }
}

interface ServerPairCountQueryDispatcher : PairCountQuery.Dispatcher {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    override suspend fun perform(query: PairCountQuery): Int {
        val pairAssignments = pairAssignmentDocumentRepository.loadPairAssignments(query.partyId)
        return pairAssignments.elements.count { it.pairs.map(PinnedCouplingPair::toPair).toList().contains(query.pair) }
    }
}
