package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.json.partyRecordJsonKeys
import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.pinRecordJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.playerRecordJsonKeys
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

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

    fun user() = JsonUser("", "", emptySet())
        .addToQuery("user")
        .let { this }

    private inline fun <reified T> T.addToQuery(queryKey: String, inputString: String = "") {
        val queryFields = nestedKeys().toGqlQueryFields()
        queries.add("$queryKey$inputString $queryFields")
    }
}

inline fun <reified T> T.nestedKeys() = let(Json.Default::encodeToJsonElement)
    .jsonObject
    .nestedKeys()

data class Entry(val content: Map<String, Entry?>)

fun JsonObject.nestedKeys(): Map<String, Entry?> = keys.mapNotNull { key ->
    when (val entry = jsonObject[key]) {
        is JsonObject -> key to Entry(entry.nestedKeys())
        is JsonArray -> entry.jsonArray.let {
            key to (if (it.size > 0 && it[0] is JsonObject) Entry(it[0].jsonObject.nestedKeys()) else null)
        }

        JsonNull, null -> null
        else -> key to null
    }
}.toMap()

fun Map<String, Entry?>.toGqlQueryFields(): String = if (isEmpty()) {
    ""
} else {
    map { (key, value) ->
        if (value == null) {
            key
        } else {
            "$key ${value.content.toGqlQueryFields()} "
        }
    }.joinToString(", ")
        .let { "{ $it }" }
}
