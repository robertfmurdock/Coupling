package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import kotlin.time.Duration

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
    val boost: Record<Boost>? = null,
    val pairs: List<PlayerPair>? = null,
    val pair: PlayerPair? = null,
    val medianSpinDuration: Duration? = null,
    val spinsUntilFullRotation: Int? = null,
    val contributionReport: ContributionReport? = null,
)

data class ContributionReport(
    val partyId: PartyId? = null,
    val contributions: List<PartyRecord<Contribution>>? = null,
    val count: Int? = null,
    val medianCycleTime: Duration? = null,
    val withCycleTimeCount: Int? = null,
    val contributors: List<Contributor>? = null,
)

data class Contributor(
    val email: String? = null,
    val playerId: String? = null,
    val details: PartyRecord<Player>? = null,
)
