package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonConfig
import com.zegreatrob.coupling.json.JsonGlobalStatsInput
import com.zegreatrob.coupling.json.PartyInput
import com.zegreatrob.coupling.json.nestedKeys
import com.zegreatrob.coupling.json.toGqlQueryFields
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

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

    fun user(block: UserQueryBuilder.() -> Unit) = UserQueryBuilder()
        .also(block)
        .output
        .addToQuery("user")

    fun partyList(block: PartyQueryBuilder.() -> Unit) = PartyQueryBuilder()
        .also(block)
        .output
        .addToQuery("partyList")

    fun party(id: PartyId, block: PartyQueryBuilder.() -> Unit) = PartyQueryBuilder()
        .also(block)
        .output
        .addToQuery(
            "party",
            InputSettings(PartyInput(id.value), "input", "PartyInput"),
        )

    fun globalStats(year: Int) {
        GqlReference.globalStats.addToQuery(
            queryKey = "globalStats",
            inputSettings = InputSettings(
                input = JsonGlobalStatsInput(year = year),
                inputName = "input",
                inputType = "GlobalStatsInput",
            ),
        )
    }

    fun config(block: ConfigQueryBuilder.() -> Unit) = ConfigQueryBuilder()
        .also(block)
        .output
        .addToQuery("config")
}

class ConfigQueryBuilder : QueryBuilder<JsonConfig> {
    override var output: JsonConfig = JsonConfig()

    fun addToSlackUrl() {
        output = output.copy(addToSlackUrl = "")
    }

    fun discordClientId() {
        output = output.copy(discordClientId = "")
    }
}
