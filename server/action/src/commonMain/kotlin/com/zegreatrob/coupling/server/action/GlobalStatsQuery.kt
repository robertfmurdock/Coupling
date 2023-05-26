package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyStats
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
            val partyStatsFlow = partyRepository.loadParties()
                .asFlow()
                .map {
                    it to pairAssignmentDocumentRepository.loadPairAssignments(it.data.id)
                }.filter { (_, docs) ->
                    docs.any { it.data.element.date.year.year == query.year }
                }.map { (party, docs) ->
                    PartyStats(
                        name = party.data.name ?: party.data.id.value,
                        id = party.data.id,
                        playerCount = 0,
                        spins = docs.size,
                    )
                }
            return GlobalStats(parties = partyStatsFlow.toList()).successResult()
        }
    }
}
