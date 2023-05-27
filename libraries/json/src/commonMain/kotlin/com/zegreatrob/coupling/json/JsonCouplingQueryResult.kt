package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import korlibs.time.TimeSpan
import korlibs.time.toTimeString
import kotlinx.serialization.Serializable

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonPartyRecord>? = null,
    val user: JsonUser? = null,
    val partyData: JsonPartyData? = null,
    val globalStats: JsonGlobalStats? = null,
)

private fun JsonPartyData.toModel() = PartyData(
    id = id?.let(::PartyId),
    party = party?.toModelRecord(),
    pinList = pinList?.map(JsonPinRecord::toModel),
    playerList = playerList?.map(JsonPlayerRecord::toModel),
    retiredPlayers = retiredPlayers?.map(JsonPlayerRecord::toModel),
    pairAssignmentDocumentList = pairAssignmentDocumentList?.map(JsonPairAssignmentDocumentRecord::toModel),
    currentPairAssignmentDocument = currentPairAssignmentDocument?.toModel(),
)

fun JsonCouplingQueryResult.toDomain() = CouplingQueryResult(
    partyList = partyList?.map(JsonPartyRecord::toModelRecord),
    user = user?.toModel(),
    partyData = partyData?.toModel(),
    globalStats = globalStats?.toModel(),
)

data class CouplingQueryResult(
    val partyList: List<Record<Party>>? = null,
    val user: User? = null,
    val partyData: PartyData? = null,
    val globalStats: GlobalStats? = null,
)

@Serializable
data class JsonPartyData(
    val id: String? = null,
    val party: JsonPartyRecord? = null,
    val pinList: List<JsonPinRecord>? = null,
    val playerList: List<JsonPlayerRecord>? = null,
    val retiredPlayers: List<JsonPlayerRecord>? = null,
    val pairAssignmentDocumentList: List<JsonPairAssignmentDocumentRecord>? = null,
    val currentPairAssignmentDocument: JsonPairAssignmentDocumentRecord? = null,
)

@Serializable
data class JsonGlobalStats(
    val parties: List<JsonPartyStats>,
)

fun JsonGlobalStats.toModel() = GlobalStats(
    parties = parties.map(JsonPartyStats::toModel),
)

fun GlobalStats.toJson() = JsonGlobalStats(
    parties = parties.map(PartyStats::toJson),
)

@Serializable
data class JsonGlobalStatsInput(
    val year: Int,
)

@Serializable
data class JsonPartyStats(
    val name: String,
    val id: String,
    val playerCount: Int,
    val spins: Int,
    val medianSpinDurationMillis: Double?,
    val medianSpinDuration: String? = null,
)

fun JsonPartyStats.toModel() = PartyStats(
    name = name,
    id = PartyId(id),
    playerCount = playerCount,
    spins = spins,
    medianSpinDuration = medianSpinDurationMillis?.let(::TimeSpan),
)

fun PartyStats.toJson() = JsonPartyStats(
    name = name,
    id = id.value,
    playerCount = playerCount,
    spins = spins,
    medianSpinDuration = medianSpinDuration?.toTimeString(components = 4),
    medianSpinDurationMillis = medianSpinDuration?.milliseconds,
)

data class PartyData(
    val id: PartyId? = null,
    val party: Record<Party>? = null,
    val pinList: List<PartyRecord<Pin>>? = null,
    val playerList: List<PartyRecord<Player>>? = null,
    val retiredPlayers: List<PartyRecord<Player>>? = null,
    val pairAssignmentDocumentList: List<PartyRecord<PairAssignmentDocument>>? = null,
    val currentPairAssignmentDocument: PartyRecord<PairAssignmentDocument>? = null,
)
