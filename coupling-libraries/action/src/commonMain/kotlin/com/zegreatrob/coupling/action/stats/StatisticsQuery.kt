package com.zegreatrob.coupling.action.stats

import com.zegreatrob.coupling.action.StatisticsReport
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class StatisticsQuery(val partyId: PartyId) :
    SimpleSuspendAction<StatisticsQuery.Dispatcher, StatisticsQuery.Results?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: StatisticsQuery): Results?
    }

    data class Results(
        val party: Party,
        val players: List<Player>,
        val history: List<PairAssignmentDocument>,
        val report: StatisticsReport,
        val heatmapData: List<List<Double?>>,
    )
}
