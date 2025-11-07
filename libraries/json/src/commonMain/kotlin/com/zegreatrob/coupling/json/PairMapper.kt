package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPair.toModel() = PlayerPair(
    players = players.map(GqlPlayer::toModel),
    count = count,
    spinsSinceLastPaired = spinsSinceLastPaired,
    recentTimesPaired = recentTimesPaired,
    pairAssignmentHistory = pairAssignmentHistory.map { json ->
        PairAssignment(
            documentId = json.id,
            details = json.toModel(),
            date = json.date,
            allPairs = json.pairs.map(GqlPinnedPair::toModel).toNotEmptyList().getOrNull(),
            recentTimesPaired = null,
            playerIds = emptyList(),
        )
    },
    contributionReport = contributionReport?.toModel(),
)

fun PartyElement<PlayerPair>.toJson() = GqlPair(
    players = element.players.map(PartyRecord<Player>::toSerializable),
    spinsSinceLastPaired = element.spinsSinceLastPaired,
    partyId = partyId,
    count = null,
    pairAssignmentHistory = emptyList(),
    recentTimesPaired = null,
    contributionReport = null,
)
