package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.coupling.cdnLookup.external.readpkgup.readPkgUp
import com.zegreatrob.coupling.cdnLookup.external.resolvepkg.resolvePkg
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

suspend fun getVersionForLibrary(lib: String): String {
    val libPackage = resolvePkg(lib, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).await()
    return pkg["pkg"].unsafeCast<Json>()["version"].unsafeCast<String>()
}
