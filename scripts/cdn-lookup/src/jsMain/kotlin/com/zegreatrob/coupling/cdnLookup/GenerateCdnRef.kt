package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.coupling.cdnLookup.external.readpkgup.readPkgUp
import com.zegreatrob.coupling.cdnLookup.external.resolvepkg.resolvePkg
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

suspend fun generateCdnRef(cdnLibs: List<String>): List<Pair<String, String>> = coroutineScope {
    cdnLibs.map { lib -> async { lookupCdnUrl(lib) } }.awaitAll()
}

private suspend fun lookupCdnUrl(lib: String): Pair<String, String> {
    val version = getVersionForLibrary(lib)

    val split = lib.indexOf("/")
    val (module, submodule) = if (lib.startsWith("@") || split < 0) lib to "" else lib.take(split) to lib.substring(split)

    return lib to "https://esm.sh/$module@$version$submodule"
}

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
