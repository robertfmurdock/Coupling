package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.coupling.cdnLookup.external.readpkgup.readPkgUp
import com.zegreatrob.coupling.cdnLookup.external.resolvepkg.resolvePkg
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Json
import kotlin.js.json

val contextPath = js("__dirname").unsafeCast<String>()

val httpClient = HttpClient {
    install(ContentNegotiation) { json() }
    install(ContentEncoding) { gzip() }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 5)
        exponentialDelay()
    }
}

suspend fun generateCdnRef(cdnLibs: List<String>): List<Pair<String, String>> = coroutineScope {
    cdnLibs.map { lib -> async { lookupCdnUrl(lib) } }.awaitAll()
}

private suspend fun lookupCdnUrl(lib: String): Pair<String, String> {
    val version = getVersionForLibrary(lib)
    val filename = lookupCdnFilename(lib, version)
    return lib to "https://cdn.jsdelivr.net/npm/$lib@$version/$filename"
}

private suspend fun lookupCdnFilename(lib: String, version: String): String {
    val cdnLibraryDescription =
        httpClient.get("https://data.jsdelivr.com/v1/package/npm/$lib@$version").body<JsonObject>()
    val files = cdnLibraryDescription.defaultFile() + cdnLibraryDescription.getFiles()
    return files.firstOrNull { fileName -> fileName.contains("umd") && fileName.endsWith(".production.min.js") }
        ?: files.firstOrNull { fileName -> fileName.endsWith(".min.js") && !fileName.contains("cjs") }
        ?: files.first()
}

private fun JsonObject.defaultFile() = listOfNotNull(this["default"]?.jsonPrimitive?.content?.substring(1))

private fun JsonObject.getFiles(): List<String> {
    return this["files"]?.jsonArray?.flatMap { thing: JsonElement ->
        val jsonObject = thing.jsonObject
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
        return@flatMap listOf(name) + jsonObject.getFiles().map { "$name/$it" }
    } ?: emptyList()
}

suspend fun getVersionForLibrary(lib: String): String {
    val libPackage = resolvePkg(lib, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).await()
    return pkg["pkg"].unsafeCast<Json>()["version"].unsafeCast<String>()
}
