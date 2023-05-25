package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class JsonCouplingQueryResult(
    val partyList: List<JsonPartyRecord>? = null,
    val user: JsonUser? = null,
    val partyData: JsonPartyData? = null,
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
)

data class CouplingQueryResult(
    val partyList: List<Record<Party>>? = null,
    val user: User? = null,
    val partyData: PartyData? = null,
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

data class PartyData(
    val id: PartyId? = null,
    val party: Record<Party>? = null,
    val pinList: List<PartyRecord<Pin>>? = null,
    val playerList: List<PartyRecord<Player>>? = null,
    val retiredPlayers: List<PartyRecord<Player>>? = null,
    val pairAssignmentDocumentList: List<PartyRecord<PairAssignmentDocument>>? = null,
    val currentPairAssignmentDocument: PartyRecord<PairAssignmentDocument>? = null,
)
