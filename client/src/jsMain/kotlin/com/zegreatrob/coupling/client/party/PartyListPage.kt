package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.schema.PartyListQuery
import com.zegreatrob.coupling.client.schema.fragment.PartyDetailsFragment
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy
import org.kotools.types.ExperimentalKotoolsTypesApi

@Lazy
val PartyListPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = query(),
    ) { _, _, result ->
        val partyDetailsList =
            result.partyList?.mapNotNull { it.details?.partyDetailsFragment?.toModel() } ?: emptyList()
        PartyList(partyDetailsList)
    }
}

private fun query(): ApolloGraphQuery<PartyListQuery.Data> = ApolloGraphQuery(PartyListQuery())

@OptIn(ExperimentalKotoolsTypesApi::class)
fun PartyDetailsFragment.toModel(): PartyDetails = PartyDetails(
    id = id,
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    email = email,
    name = name,
    badgesEnabled = badgesEnabled == true,
    callSignsEnabled = callSignsEnabled == true,
    animationEnabled = animationsEnabled != false,
    animationSpeed = animationSpeed ?: 1.0,
)
