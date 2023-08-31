package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonConfig
import com.zegreatrob.coupling.json.JsonGlobalStatsInput
import com.zegreatrob.coupling.json.PartyInput
import com.zegreatrob.coupling.json.nestedKeys
import com.zegreatrob.coupling.json.toGqlQueryFields
import com.zegreatrob.coupling.json.toQueryLines
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

@CouplingQueryDsl
class CouplingQueryBuilder : BuilderWithInput() {
    override var queries = mutableListOf<String>()
    override var inputs = mutableListOf<String>()
    override var variables = mutableMapOf<String, JsonElement>()

    fun build(): Pair<String, JsonObject> {
        val args = if (inputs.isEmpty()) "" else "(${inputs.joinToString(",")})"
        return Pair(
            "query $args {\n${queries.joinToString("\n")}\n}",
            variablesJson(),
        )
    }

    private fun variablesJson() = buildJsonObject { variables.forEach { put(it.key, it.value) } }

    fun user(block: UserQueryBuilder.() -> Unit) = UserQueryBuilder()
        .also(block)
        .output
        .addToQuery("user")

    fun partyList(block: PartyQueryBuilder.() -> Unit) = PartyQueryBuilder()
        .also(block)
        .mergeToParent()
        .output
        .addToQuery("partyList")

    fun party(id: PartyId, block: PartyQueryBuilder.() -> Unit) = mergeToParent(
        PartyQueryBuilder()
            .also(block),
        InputSettings(PartyInput(id.value), "partyInput", "PartyInput"),
        "party",
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

abstract class BuilderWithInput {
    abstract var queries: MutableList<String>
    abstract var inputs: MutableList<String>
    abstract var variables: MutableMap<String, JsonElement>

    inline fun <reified I> InputSettings<I>.addInputString() = "(input: \$$inputName)".also {
        inputs.add("\$$inputName: $inputType!")
        variables[inputName] = Json.encodeToJsonElement(input)
    }

    inline fun <T, reified J, reified I> mergeToParent(
        child: T,
        inputSettings: InputSettings<I>,
        queryKey: String,
    ): T where T : BuilderWithInput, T : QueryBuilder<J> {
        this.inputs.addAll(child.inputs)
        this.variables.putAll(child.variables)
        queries.add("$queryKey${inputSettings.addInputString<I>()} ${queryContent<J, T>(child)}")
        return child
    }

    inline fun <reified J, T> queryContent(child: T): String where T : BuilderWithInput, T : QueryBuilder<J> =
        child.output.nestedKeys<J>()
            .toQueryLines()
            .plus(child.queries)
            .joinToString(", ")
            .let { "{ $it }" }

    inline fun <T, reified J> T.mergeToParent(): T where T : BuilderWithInput, T : QueryBuilder<J> {
        this@BuilderWithInput.inputs.addAll(this@mergeToParent.inputs)
        this@BuilderWithInput.variables.putAll(this@mergeToParent.variables)
        output.addToQuery("party")
        return this@mergeToParent
    }

    inline fun <reified T> T.addToQuery(queryKey: String, inputString: String = "") {
        val queryFields = nestedKeys().toGqlQueryFields()
        queries.add("$queryKey$inputString $queryFields")
    }

    inline fun <reified T, reified I> T.addToQuery(
        queryKey: String,
        inputSettings: InputSettings<I>,
    ) = addToQuery(
        queryKey = queryKey,
        inputString = inputSettings.addInputString(),
    )
}

class ConfigQueryBuilder : QueryBuilder<JsonConfig> {
    override var output: JsonConfig = JsonConfig()

    fun addToSlackUrl() {
        output = output.copy(addToSlackUrl = "")
    }

    fun discordClientId() {
        output = output.copy(discordClientId = "")
    }

    fun stripeAdminCode() {
        output = output.copy(stripeAdminCode = "")
    }

    fun stripePurchaseCode() {
        output = output.copy(stripePurchaseCode = "")
    }
}
