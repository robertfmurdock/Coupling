package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.sources.ValueSource
import com.zegreatrob.coupling.json.toJsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ConfigFileSource(val envvarReader: (key: String) -> String?) : ValueSource {
    val config = getConfigFromFile()
    val configJsonElement = config?.toJsonElement<CouplingCliConfig>()

    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        println("hi values option $option")
        val configAsElement = configJsonElement
            ?: return emptyList()
        return findInvocations(configAsElement, option)
    }

    private fun findInvocations(
        configAsElement: JsonElement,
        option: Option,
    ): List<ValueSource.Invocation> {
        var cursor: JsonElement? = configAsElement
        val parts = option.parts()
        for (part in parts) {
            if (cursor !is JsonObject) return emptyList()
            cursor = cursor[part]
        }
        if (cursor == null) return emptyList()

        try {
            if (cursor is JsonArray) {
                return cursor.map {
                    ValueSource.Invocation.value(it.jsonPrimitive.content)
                }
            }
            return ValueSource.Invocation.just(cursor.jsonPrimitive.content)
        } catch (_: IllegalArgumentException) {
            return emptyList()
        }
    }

    private fun Option.parts(): List<String> = valueSourceKey?.split(".")
        ?: listOf(ValueSource.name(this).kebabToCamelCase())

    private fun String.kebabToCamelCase(): String {
        val pattern = "-[a-z]".toRegex()
        return replace(pattern) { it.value.last().uppercase() }
    }

    private fun getConfigFromFile(): CouplingCliConfig? {
        val pwd = envvarReader("PWD")
        val fileContents = readFromFile("$pwd/.coupling")
            ?: return null
        return Json.decodeFromString<CouplingCliConfig>(fileContents)
    }
}
