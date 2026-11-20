package com.zegreatrob.coupling.client.contribution

import com.apollographql.apollo.api.Optional
import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionOverviewContent
import com.zegreatrob.coupling.client.components.contribution.ContributionQuickStatsPanel
import com.zegreatrob.coupling.client.components.contribution.ContributionStartContent
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.gql.ContributionOverviewPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.type.ContributionsInput
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import js.lazy.Lazy
import react.Key

@Lazy
val ContributionOverviewPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(
            ContributionOverviewPageQuery(
                partyInput = PartyInput(partyId),
                contributionReportInput = ContributionsInput(limit = Optional.present(5)),
            ),
        ),
        key = Key(partyId.value.toString()),
    ) { _, dispatchFunc, queryResult ->
        val party = queryResult.party?.partyDetails?.toDomain()
            ?: return@CouplingQuery
        val report = queryResult.party.contributionReport ?: return@CouplingQuery
        val contributions = report.contributions.map { it.contributionDetails.toDomain() }
        val contributorCount = contributions.flatMap { it.participantEmails }.toSet().size
        val players = queryResult.party.playerList.map { it.playerDetails.toDomain() }
        val retiredPlayers = queryResult.party.retiredPlayers.map { it.playerDetails.toDomain() }
        UpdatingPlayerList(
            players = players + retiredPlayers,
            dispatchFunc = dispatchFunc,
        ) { playerList, dispatchFunc ->
            ContributionContentFrame(party = party) {
                ContributionQuickStatsPanel(
                    contributionCount = report.count,
                    medianCycleTime = report.medianCycleTime,
                    cycleTimeCount = report.withCycleTimeCount ?: 0,
                    contributorCount = contributorCount,
                )
                if (contributions.isEmpty()) {
                    ContributionStartContent(party)
                } else {
                    ContributionOverviewContent(party, contributions, playerList, dispatchFunc)
                }
            }
        }
    }
}
