package com.zegreatrob.coupling.cdnLookup

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

fun main() {
    val arguments = processArguments().toList()
    val lookupConfig = processLookupConfig(arguments)
    val libs = arguments.filterNot { it.startsWith("--lookup-config-base64=") }
    MainScope().launch {
        generateCdnRef(libs, lookupConfig)
            .toJson()
            .let(::println)
    }.invokeOnCompletion { cause: Throwable? ->
        if (cause != null) {
            js("process.exit(-1)")
        }
    }
}

private fun List<Pair<String, String>>.toJson() = Json.encodeToJsonElement(toMap())

private fun processArguments() = js("process.argv.splice(2)").unsafeCast<Array<String>>()

private fun processLookupConfig(arguments: List<String>): CdnLookupConfig {
    val encodedConfig = arguments
        .firstOrNull { it.startsWith("--lookup-config-base64=") }
        ?.removePrefix("--lookup-config-base64=")
        ?: return CdnLookupConfig()
    val decodedConfig = js("Buffer.from")(encodedConfig, "base64")
        .unsafeCast<dynamic>()
        .toString("utf8")
        .unsafeCast<String>()
    return Json.decodeFromString<CdnLookupConfig>(decodedConfig)
}
