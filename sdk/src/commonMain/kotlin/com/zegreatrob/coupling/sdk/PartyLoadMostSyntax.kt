package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.CouplingQueryResult
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

data class PartyDataMost(
    val party: Party,
    val playerList: List<Player>,
    val currentPairDocument: PairAssignmentDocument?,
    val pinList: List<Pin>,
)

fun CouplingQueryResult?.toPartyDataMost() = this?.partyData?.let {
    PartyDataMost(
        party = it.party?.data ?: return@let null,
        playerList = it.playerList?.elements ?: return@let null,
        pinList = it.pinList?.elements ?: return@let null,
        currentPairDocument = it.currentPairAssignmentDocument?.element,
    )
}
