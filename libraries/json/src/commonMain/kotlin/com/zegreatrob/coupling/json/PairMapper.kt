package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import kotools.types.collection.toNotEmptyList
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPair.toModel() = PlayerPair(
    players = players?.map(GqlPlayerDetails::toModel),
    count = count,
    spinsSinceLastPaired = spinsSinceLastPaired,
    recentTimesPaired = recentTimesPaired,
    pairAssignmentHistory = pairAssignmentHistory?.map { json ->
        PairAssignment(
            documentId = json.documentId?.let { PairAssignmentDocumentId(NotBlankString.create(it)) },
            details = json.details?.toModel(),
            date = json.date,
            allPairs = json.allPairs?.map(GqlPinnedPair::toModel)?.toNotEmptyList()?.getOrNull(),
            recentTimesPaired = json.recentTimesPaired,
        )
    },
    contributionReport = contributionReport?.toModel(),
)

fun PartyElement<PlayerPair>.toJson() = GqlPair(
    players = element.players?.map(PartyRecord<Player>::toSerializable),
    spinsSinceLastPaired = element.spinsSinceLastPaired,
    partyId = partyId,
    count = null,
    pairAssignmentHistory = null,
    recentTimesPaired = null,
    contributionReport = null,
)
