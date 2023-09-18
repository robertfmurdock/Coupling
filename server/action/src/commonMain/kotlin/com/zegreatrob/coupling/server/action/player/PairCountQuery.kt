package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.hasPair
import com.zegreatrob.coupling.model.pairassignmentdocument.spinsSinceLastPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairCountQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        suspend fun perform(query: PairCountQuery): Int
    }
}

@ActionMint
data class ContributorPlayerQuery(val partyId: PartyId, val email: String) {
    interface Dispatcher : PartyPlayersSyntax {
        suspend fun perform(query: ContributorPlayerQuery): PartyRecord<Player>? = query.partyId.loadPlayers()
            .find {
                listOf(it.element.email)
                    .plus(it.element.unvalidatedEmails)
                    .map(String::lowercase)
                    .contains(query.email)
            }
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
