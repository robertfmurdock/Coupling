package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

data class Party(
    val id: PartyId? = null,
    val details: Record<PartyDetails>? = null,
    val integration: Record<PartyIntegration>? = null,
    val pinList: List<PartyRecord<Pin>>? = null,
    val playerList: List<PartyRecord<Player>>? = null,
    val retiredPlayers: List<PartyRecord<Player>>? = null,
    val secretList: List<PartyRecord<Secret>>? = null,
    val pairAssignmentDocumentList: List<PartyRecord<PairAssignmentDocument>>? = null,
    val currentPairAssignmentDocument: PartyRecord<PairAssignmentDocument>? = null,
)