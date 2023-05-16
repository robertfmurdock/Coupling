package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.json.nestedKeys
import com.zegreatrob.coupling.json.partyRecordJsonKeys
import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.pinRecordJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.playerRecordJsonKeys
import com.zegreatrob.coupling.json.toGqlQueryFields
import com.zegreatrob.coupling.model.party.PartyId
import korlibs.time.DateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

enum class PartyGQLComponent(val value: String) {
    PartyData(queryAllFields("party", partyRecordJsonKeys)),
    PlayerList(queryAllFields("playerList", playerRecordJsonKeys)),
    RetiredPlayerList(queryAllFields("retiredPlayers", playerRecordJsonKeys)),
    PinList(queryAllFields("pinList", pinRecordJsonKeys)),
    PairAssignmentDocumentList(
        "pairAssignmentDocumentList {id,date,partyId,isDeleted,modifyingUserEmail,timestamp," +
            "pairs { " +
            "players {" +
            "${playerJsonKeys.joinToString(",")}, " +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "}",
    ),
    CurrentPairAssignmentDocument(
        "currentPairAssignmentDocument {id,date,partyId,isDeleted,modifyingUserEmail,timestamp," +
            "pairs { " +
            "players {" +
            "${playerJsonKeys.joinToString(",")}, " +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "}",
    ),
}

private fun queryAllFields(name: String, keys: Set<String>) = "$name {${keys.joinToString(",")}}"

@DslMarker
annotation class CouplingQueryDsl

@CouplingQueryDsl
class CouplingQueryBuilder {
    private var queries = mutableListOf<String>()
    private var inputs = mutableListOf<String>()
    private var variables = mutableMapOf<String, JsonElement>()

    fun variablesJson() = buildJsonObject { variables.forEach { put(it.key, it.value) } }

    fun build(): String {
        val args = if (inputs.isEmpty()) "" else "(${inputs.joinToString(",")})"
        return "query $args {\n${queries.joinToString("\n")}\n}"
    }

    private inline fun <reified T> T.addToQuery(queryKey: String, inputString: String = "") {
        val queryFields = nestedKeys().toGqlQueryFields()
        queries.add("$queryKey$inputString $queryFields")
    }

    fun user() = apply { referenceUser().addToQuery("user") }

    fun partyList() = apply { referencePartyRecord().addToQuery("partyList") }
}

private fun referenceUser() = JsonUser("", "", emptySet())

private fun referencePartyRecord() = JsonPartyRecord(
    id = PartyId(""),
    pairingRule = 0,
    badgesEnabled = false,
    defaultBadgeName = "",
    alternateBadgeName = "",
    email = "",
    name = "",
    callSignsEnabled = false,
    animationsEnabled = false,
    animationSpeed = 0.0,
    modifyingUserEmail = "",
    isDeleted = false,
    timestamp = DateTime.EPOCH,
)
