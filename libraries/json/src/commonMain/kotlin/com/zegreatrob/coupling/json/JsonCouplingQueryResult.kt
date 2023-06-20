package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyData
import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.party.PartyId
import korlibs.time.TimeSpan
import korlibs.time.toTimeString
import kotlinx.serialization.Serializable

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonPartyDetailsRecord>? = null,
    val user: JsonUser? = null,
    val party: JsonParty? = null,
    val globalStats: JsonGlobalStats? = null,
)

@Serializable
data class PartyInput(
    val partyId: String,
)

private fun JsonParty.toModel() = PartyData(
    id = id.let(::PartyId),
    party = details?.toModelRecord(),
    pinList = pinList?.map(JsonPinRecord::toModel),
    playerList = playerList?.map(JsonPlayerRecord::toModel),
    secretList = secretList?.map(JsonSecretRecord::toModel),
    retiredPlayers = retiredPlayers?.map(JsonPlayerRecord::toModel),
    pairAssignmentDocumentList = pairAssignmentDocumentList?.map(JsonPairAssignmentDocumentRecord::toModel),
    currentPairAssignmentDocument = currentPairAssignmentDocument?.toModel(),
)

fun JsonCouplingQueryResult.toDomain() = CouplingQueryResult(
    partyList = partyList?.map(JsonPartyDetailsRecord::toModelRecord),
    user = user?.toModel(),
    partyData = party?.toModel(),
    globalStats = globalStats?.toModel(),
)

@Serializable
data class JsonParty(
    val id: String,
    val details: JsonPartyDetailsRecord? = null,
    val pinList: List<JsonPinRecord>? = null,
    val playerList: List<JsonPlayerRecord>? = null,
    val secretList: List<JsonSecretRecord>? = null,
    val retiredPlayers: List<JsonPlayerRecord>? = null,
    val pairAssignmentDocumentList: List<JsonPairAssignmentDocumentRecord>? = null,
    val currentPairAssignmentDocument: JsonPairAssignmentDocumentRecord? = null,
)

@Serializable
data class JsonGlobalStats(
    val parties: List<JsonPartyStats>,
    val totalParties: Int,
    val totalSpins: Int,
    val totalPlayers: Int,
    val totalAppliedPins: Int,
    val totalUniquePins: Int,
)

fun JsonGlobalStats.toModel() = GlobalStats(
    parties = parties.map(JsonPartyStats::toModel),
    totalSpins = totalSpins,
    totalPlayers = totalPlayers,
    totalAppliedPins = totalAppliedPins,
    totalUniquePins = totalUniquePins,
    totalParties = totalParties,
)

fun GlobalStats.toJson() = JsonGlobalStats(
    parties = parties.map(PartyStats::toJson),
    totalSpins = totalSpins,
    totalPlayers = totalPlayers,
    totalAppliedPins = totalAppliedPins,
    totalUniquePins = totalUniquePins,
    totalParties = totalParties,
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
    val appliedPinCount: Int,
    val uniquePinCount: Int,
)

fun JsonPartyStats.toModel() = PartyStats(
    name = name,
    id = PartyId(id),
    playerCount = playerCount,
    spins = spins,
    medianSpinDuration = medianSpinDurationMillis?.let(::TimeSpan),
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
)

fun PartyStats.toJson() = JsonPartyStats(
    name = name,
    id = id.value,
    playerCount = playerCount,
    appliedPinCount = appliedPinCount,
    uniquePinCount = uniquePinCount,
    spins = spins,
    medianSpinDuration = medianSpinDuration?.toTimeString(components = 4),
    medianSpinDurationMillis = medianSpinDuration?.milliseconds,
)
