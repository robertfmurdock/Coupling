package com.zegreatrob.coupling.json

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
    val user: List<JsonUserRecord>? = null,
    val partyData: List<JsonPartyData>? = null,
)

fun JsonCouplingQueryResult.toDomain() = CouplingQueryResult(
    partyList = partyList?.map(JsonPartyRecord::toModelRecord),
)

data class CouplingQueryResult(
    val partyList: List<Record<Party>>? = null,
    val user: User? = null,
    val partyData: PartyData? = null,
)

@Serializable
data class JsonPartyData(
    val id: String,
    val party: JsonPartyRecord? = null,
    val pinList: List<JsonPinRecord>? = null,
    val playerList: List<JsonPlayerRecord>? = null,
    val retiredPlayers: List<JsonPlayerRecord>? = null,
    val pairAssignmentDocumentList: List<JsonPairAssignmentDocumentRecord>? = null,
    val currentPairAssignmentDocument: JsonPairAssignmentDocumentRecord? = null,
)

data class PartyData(
    val id: PartyId,
    val party: Record<Party>? = null,
    val pinList: List<Record<Pin>>? = null,
    val playerList: List<Record<Player>>? = null,
    val retiredPlayers: List<Record<Player>>? = null,
    val pairAssignmentDocumentList: List<Record<PairAssignmentDocument>>? = null,
    val currentPairAssignmentDocument: Record<PairAssignmentDocument>? = null,
)
