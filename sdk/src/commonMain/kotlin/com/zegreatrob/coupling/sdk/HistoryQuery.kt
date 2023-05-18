package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.CouplingQueryResult
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId

typealias HistoryData = Pair<Party, List<PairAssignmentDocument>>

fun HistoryQuery(partyId: PartyId) = graphQuery {
    party(partyId) {
        party()
        pairAssignmentDocumentList()
    }
}

fun CouplingQueryResult?.toHistoryData(): HistoryData? {
    return this?.partyData?.let {
        Pair(
            first = it.party?.data ?: return@let null,
            second = it.pairAssignmentDocumentList?.elements ?: return@let null,
        )
    }
}
