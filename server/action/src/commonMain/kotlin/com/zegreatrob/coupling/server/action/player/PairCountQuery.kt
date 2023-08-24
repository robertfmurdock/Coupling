package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.hasPair
import com.zegreatrob.coupling.model.pairassignmentdocument.spinsSinceLastPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairCountQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        suspend fun perform(query: PairCountQuery): Int
    }
}

@ActionMint
data class PairAssignmentHistoryQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax {
        suspend fun perform(query: PairAssignmentHistoryQuery) = query.partyId.loadPairAssignmentRecords()
            .filter { it.data.element.hasPair(query.pair) }
            .let { query.pair to it }
    }
}

interface ServerPairCountQueryDispatcher : PairCountQuery.Dispatcher {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    override suspend fun perform(query: PairCountQuery): Int {
        val pairAssignments = pairAssignmentDocumentRepository.loadPairAssignments(query.partyId)
        return pairAssignments.elements.count { it.hasPair(query.pair) }
    }
}

@ActionMint
data class SpinsSinceLastPairedQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        suspend fun perform(query: SpinsSinceLastPairedQuery): Int?
    }
}

interface ServerSpinsSinceLastPairedQueryDispatcher : SpinsSinceLastPairedQuery.Dispatcher {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    override suspend fun perform(query: SpinsSinceLastPairedQuery): Int? {
        val pairAssignments = pairAssignmentDocumentRepository.loadPairAssignments(query.partyId)
        return pairAssignments.spinsSinceLastPair(query.pair)
    }
}
