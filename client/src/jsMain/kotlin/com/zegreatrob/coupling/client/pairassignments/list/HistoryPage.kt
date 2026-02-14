package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.gql.HistoryPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import js.lazy.Lazy
import react.Key

@Lazy
val HistoryPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(HistoryPageQuery(PartyInput(partyId))),
        key = Key(partyId.value.toString()),
    ) { reload, commandFunc, result ->
        val (party, history) = result.toHistoryData()
            ?: return@CouplingQuery
        History(party, history, Controls(commandFunc, reload))
    }
}

private fun HistoryPageQuery.Data.toHistoryData(): Pair<PartyDetails, List<PairingSet>>? = party?.let {
    Pair(
        first = it.partyDetails.toDomain(),
        second = it.pairingSetList.map { doc ->
            doc.pairingSetDetails.toDomain()
        },
    )
}
