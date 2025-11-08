package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import kotools.types.collection.NotEmptyList
import kotlin.time.Instant

data class PairingSet(
    val id: PairingSetId,
    val date: Instant,
    val pairs: NotEmptyList<PinnedCouplingPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

fun PairingSet.orderedPairedPlayers(): List<Player> = pairs
    .map(PinnedCouplingPair::players)
    .map(NotEmptyList<Player>::toList)
    .toList()
    .flatten()

val PartyElement<PairingSet>.document get() = element

fun List<PartyRecord<PairingSet>>.spinsSinceLastPair(couplingPair: CouplingPair) = indexOfFirst { it.element.hasPair(couplingPair) }
    .takeIf { it != -1 }

fun PairingSet.hasPair(pair: CouplingPair) = pairs.toList().any { areEqualPairs(pair, it.toPair()) }
