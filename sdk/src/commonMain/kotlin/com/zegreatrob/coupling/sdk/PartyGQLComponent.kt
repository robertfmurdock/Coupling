package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPartyData
import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.JsonPinRecord
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

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

    fun build(): Pair<String, JsonObject> {
        val args = if (inputs.isEmpty()) "" else "(${inputs.joinToString(",")})"
        return Pair(
            "query $args {\n${queries.joinToString("\n")}\n}",
            variablesJson(),
        )
    }

    private fun variablesJson() = buildJsonObject { variables.forEach { put(it.key, it.value) } }

    private inline fun <reified T> T.addToQuery(queryKey: String, inputString: String = "") {
        val queryFields = nestedKeys().toGqlQueryFields()
        queries.add("$queryKey$inputString $queryFields")
    }

    private inline fun <reified T, reified I> T.addToQuery(
        queryKey: String,
        inputSettings: InputSettings<I>,
    ) = addToQuery(
        queryKey = queryKey,
        inputString = inputSettings.addInputString(),
    )

    private inline fun <reified I> InputSettings<I>.addInputString() = "(input: \$$inputName)".also {
        inputs.add("\$$inputName: $inputType!")
        variables[inputName] = Json.encodeToJsonElement(input)
    }

    fun user() = apply { referenceUser().addToQuery("user") }

    fun partyList() = apply { referencePartyRecord().addToQuery("partyList") }

    fun party(id: PartyId, block: PartyQueryBuilder.() -> Unit) = PartyQueryBuilder()
        .also(block)
        .output
        .addToQuery(
            "partyData",
            InputSettings(id.value, "input", "String"),
        )
}

data class InputSettings<I>(val input: I, val inputName: String, val inputType: String)

class PartyQueryBuilder : QueryBuilder<JsonPartyData> {

    override var output: JsonPartyData = JsonPartyData()

    fun pinList() {
        output = output.copy(
            pinList = listOf(referencePin()),
        )
    }
}

@CouplingQueryDsl
interface QueryBuilder<T> {
    val output: T
}

private fun referencePin() = JsonPinRecord(
    id = "",
    name = "",
    icon = "",
    partyId = PartyId(""),
    modifyingUserEmail = "",
    isDeleted = false,
    timestamp = DateTime.EPOCH,
)

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
