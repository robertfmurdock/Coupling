package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.party.PartyId

data class GlobalStatsQuery(val year: Int) : SimpleSuspendResultAction<GlobalStatsQuery.Dispatcher, GlobalStats> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        fun perform(query: GlobalStatsQuery): Result<GlobalStats> =
            GlobalStats(
                listOf(
                    PartyStats(
                        name = "${query.year}",
                        id = PartyId(""),
                        playerCount = query.year,
                        spins = query.year,
                    ),
                ),
            ).successResult()
    }
}
