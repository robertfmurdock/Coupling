package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.JsonPartyData
import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.JsonPinData
import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.JsonPinnedCouplingPair
import com.zegreatrob.coupling.json.JsonPinnedPlayer
import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.json.nestedKeys
import com.zegreatrob.coupling.json.toGqlQueryFields
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import korlibs.time.DateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

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
            pinList = listOf(referencePinRecord()),
        )
    }

    fun party() {
        output = output.copy(
            party = referencePartyRecord(),
        )
    }

    fun playerList() {
        output = output.copy(
            playerList = listOf(referencePlayerRecord()),
        )
    }

    fun retiredPlayers() {
        output = output.copy(
            retiredPlayers = listOf(referencePlayerRecord()),
        )
    }

    fun currentPairAssignments() {
        output = output.copy(
            currentPairAssignmentDocument = referencePairAssignmentRecord(),
        )
    }

    fun pairAssignmentDocumentList() {
        output = output.copy(
            pairAssignmentDocumentList = listOf(referencePairAssignmentRecord()),
        )
    }
}

@CouplingQueryDsl
interface QueryBuilder<T> {
    val output: T
}

private fun referencePinRecord() = JsonPinRecord(
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

private fun referencePlayerRecord() = JsonPlayerRecord(
    id = "",
    name = "",
    email = "",
    badge = "",
    callSignAdjective = "",
    callSignNoun = "",
    imageURL = "",
    avatarType = "",
    partyId = PartyId(""),
    modifyingUserEmail = "",
    isDeleted = false,
    timestamp = DateTime.EPOCH,
)

private fun referenceJsonPinnedPlayer() = JsonPinnedPlayer(
    id = "",
    name = "",
    email = "",
    badge = "",
    callSignAdjective = "",
    callSignNoun = "",
    imageURL = "",
    avatarType = AvatarType.BoringBeam,
    pins = listOf(referenceJsonPinData()),
)

private fun referenceJsonPinnedCouplingPair() = JsonPinnedCouplingPair(
    players = listOf(referenceJsonPinnedPlayer()),
    pins = setOf(referenceJsonPinData()),
)

private fun referenceJsonPinData() = JsonPinData(
    id = "",
    name = "",
    icon = "",
)

private fun referencePairAssignmentRecord() = JsonPairAssignmentDocumentRecord(
    id = "",
    date = DateTime.EPOCH,
    pairs = listOf(referenceJsonPinnedCouplingPair()),
    partyId = PartyId(""),
    modifyingUserEmail = "",
    isDeleted = false,
    timestamp = DateTime.EPOCH,
)