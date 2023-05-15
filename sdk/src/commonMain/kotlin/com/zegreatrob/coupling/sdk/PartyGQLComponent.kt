package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.partyRecordJsonKeys
import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.pinRecordJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.playerRecordJsonKeys
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

    fun build(): String {
        val args = if (inputs.isEmpty()) "" else "(${inputs.joinToString(",")})"
        return "query $args {\n${queries.joinToString("\n")}\n}"
    }

    fun variablesJson() = buildJsonObject { variables.forEach { put(it.key, it.value) } }
}
