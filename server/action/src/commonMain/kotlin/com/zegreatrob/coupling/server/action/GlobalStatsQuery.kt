package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

data class GlobalStatsQuery(val year: Int) : SimpleSuspendResultAction<GlobalStatsQuery.Dispatcher, GlobalStats> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {

        val partyRepository: PartyRepository
        val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

        suspend fun perform(query: GlobalStatsQuery): Result<GlobalStats> {
            val matchesYear = { record: PartyRecord<PairAssignmentDocument> ->
                record.data.element.date.year.year == query.year
            }
            val partyStatsFlow = partyRepository.loadParties()
                .asFlow()
                .map { it to pairAssignmentDocumentRepository.loadPairAssignments(it.data.id) }
                .filter { (_, docs) -> docs.any(matchesYear) }
                .map { (party, docs) ->
                    val pairDocsThisYear = docs.filter(matchesYear)
                    PartyStats(
                        name = party.data.name ?: party.data.id.value,
                        id = party.data.id,
                        playerCount = pairDocsThisYear.distinctPlayersPairedThisYear().size,
                        spins = pairDocsThisYear.size,
                    )
                }
            return GlobalStats(parties = partyStatsFlow.toList()).successResult()
        }

        private fun List<PartyRecord<PairAssignmentDocument>>.distinctPlayersPairedThisYear() =
            flatMap { it.data.document.pairs.flatMap { it.players.map { it.player.id } } }
                .distinct()
    }
}
