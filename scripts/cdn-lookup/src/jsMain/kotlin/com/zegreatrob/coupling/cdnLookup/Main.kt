package com.zegreatrob.coupling.cdnLookup

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import kotlinx.serialization.json.Json
import node.buffer.BufferEncoding
import node.buffer.utf8
import node.fs.existsSync
import node.fs.readFileSync

suspend fun main() {
    CdnLookupCommand().main(processArguments())
}

private fun processArguments() = js("process.argv.slice(2)").unsafeCast<Array<String>>()

class CdnLookupCommand :
    SuspendingCliktCommand(
        name = "cdn-lookup",
    ) {
    private val lookupConfigBase64 by option(
        "--lookup-config-base64",
        hidden = true,
    )
    private val configFile by option(
        "--config",
        help = "Read lookup configuration from this JSON file.",
    ).default(".cdn-lookup.json")
    private val providerName by option(
        "--provider",
        help = "CDN provider to use.",
    ).default(DEFAULT_CDN_PROVIDER_NAME)
    private val output by option(
        "--output",
        help = "Output format to write to stdout.",
    ).default("json")
    private val libraries by argument(
        name = "libraries",
        help = "Package imports to resolve.",
    ).multiple()

    init {
        context { readArgumentFile = null }
        versionOption(CdnLookupVersions.packageVersion)
    }

    override fun help(context: Context) = "Generate pinned CDN URLs for installed npm packages."

    override suspend fun run() {
        val lookupConfig = processLookupConfig(lookupConfigBase64, configFile)
        generateCdnLookup(libraries, lookupConfig, cdnProvider = selectedProvider(providerName))
            .let { result -> formatResult(result, output, usesCompatibilityOutput = lookupConfigBase64 != null) }
            .let(::echo)
    }
}

private fun selectedProvider(providerName: String): CdnProvider = runCatching { selectedCdnProvider(providerName) }
    .getOrElse { cause ->
        throw CliktError(
            message = cause.message ?: "Unable to select CDN provider.",
            printError = true,
        )
    }

private fun formatResult(result: CdnLookupResult, output: String, usesCompatibilityOutput: Boolean): String {
    if (usesCompatibilityOutput) return result.toUrlMapJson()
    return when (output) {
        "json" -> result.toJson()
        else -> result.toJson()
    }
}

private fun processLookupConfig(encodedConfig: String?, configFile: String): CdnLookupConfig {
    val config = encodedConfig?.let(::decodeBase64) ?: readConfigFileText(configFile)
    return config?.let(::decodeLookupConfig) ?: CdnLookupConfig()
}

private fun decodeBase64(encodedConfig: String): String = js("Buffer.from")(encodedConfig, "base64")
    .unsafeCast<dynamic>()
    .toString("utf8")
    .unsafeCast<String>()

private fun readConfigFileText(configFile: String): String? = if (existsSync(configFile)) {
    readFileSync(configFile, BufferEncoding.utf8)
} else {
    null
}

private fun decodeLookupConfig(config: String): CdnLookupConfig = Json.decodeFromString<CdnLookupConfig>(config)
