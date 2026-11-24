package com.zegreatrob.coupling.cli

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import node.path.path
import node.url.fileURLToPath

actual fun couplingVersion(): String {
    var directory = path.dirname(fileURLToPath(moduleUrl()))
    while (true) {
        val manifest = readFileText(path.join(directory, "package.json"))
            ?.let { Json.parseToJsonElement(it).jsonObject }
        if (manifest != null) {
            return manifest.getValue("version").jsonPrimitive.content
        }
        val parent = path.dirname(directory)
        check(parent != directory) { "Cannot locate coupling-cli package manifest" }
        directory = parent
    }
}

private fun moduleUrl(): String = js("import.meta.url")
