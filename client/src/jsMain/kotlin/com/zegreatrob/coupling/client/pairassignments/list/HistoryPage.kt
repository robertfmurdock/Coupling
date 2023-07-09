package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.create

val HistoryPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                pairAssignmentDocumentList()
            }
        },
        toDataprops = { reload, commandFunc, result ->
            val (party, history) = result.toHistoryData()
                ?: return@CouplingQuery null
            History(party, history, Controls(commandFunc, reload))
        },
    ).create(key = partyId.value)
}

typealias HistoryData = Pair<PartyDetails, List<PairAssignmentDocument>>

fun CouplingQueryResult?.toHistoryData(): HistoryData? {
    return this?.party?.let {
        Pair(
            first = it.details?.data ?: return@let null,
            second = it.pairAssignmentDocumentList?.elements ?: return@let null,
        )
    }
}