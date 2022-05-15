package com.zegreatgrob.coupling.cdnLookup

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.await
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

val readPkgUp = js("require('read-pkg-up')")
val resolvePkg = js("require('resolve-pkg')")

val contextPath = js("__dirname").unsafeCast<String>()

val httpClient = HttpClient {
    install(ContentNegotiation) { json() }
    install(ContentEncoding) { gzip() }
}

suspend fun generateCdnRef(cdnLibs: List<String>): List<String> = cdnLibs.map { lib ->
    val version = getVersionForLibrary(lib)
    val filename = lookupCdnFilename(lib, version)
    "https://cdnjs.cloudflare.com/ajax/libs/$lib/$version/$filename"
}

private suspend fun lookupCdnFilename(lib: String, version: String): String? {
    val cdnLibraryDescription = httpClient.get("https://api.cdnjs.com/libraries/$lib").body<JsonObject>()

    return if (cdnLibraryDescription["versions"]?.jsonArray?.map { it.jsonPrimitive.content }
        ?.contains(version) == true
    ) {
        cdnLibraryDescription["filename"]?.jsonPrimitive?.content
    } else {
        ""
    }
}

private suspend fun getVersionForLibrary(lib: String): String {
    val libPackage = resolvePkg(lib, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).unsafeCast<Promise<Json>>().await()
    return pkg["pkg"].unsafeCast<Json>()["version"].unsafeCast<String>()
}
