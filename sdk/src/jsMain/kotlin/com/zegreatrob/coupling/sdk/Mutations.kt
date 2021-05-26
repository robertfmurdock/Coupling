package com.zegreatrob.coupling.sdk

import kotlin.js.Json
import kotlin.reflect.KProperty

object Mutations {
    val spin by LoadGqlFile
    val savePin by LoadGqlFile
    val saveTribe by LoadGqlFile
    val savePlayer by LoadGqlFile
    val savePairAssignments by LoadGqlFile
    val deleteTribe by LoadGqlFile
    val deletePin by LoadGqlFile
    val deletePairAssignments by LoadGqlFile
    val deletePlayer by LoadGqlFile
}

object LoadGqlFile {
    operator fun getValue(mutations: Mutations, property: KProperty<*>) = loadTextFile(property.name)

    private fun loadTextFile(path: String) = kotlinext.js.require("fs")
        ?.readFileSync.unsafeCast<((String, String) -> dynamic)?>()
        ?.let { readFileSync ->
            js("process.env").unsafeCast<Json>()["NODE_PATH"].unsafeCast<String>()
                .split(":")
                .asSequence()
                .mapNotNull { nodePath ->
                    try {
                        readFileSync("$nodePath/com/zegreatrob/coupling/sdk/$path.graphql", "utf8").unsafeCast<String?>()
                    } catch (any: Throwable) {
                        null
                    }
                }
                .first()
        }
        ?: kotlinext.js.require("com/zegreatrob/coupling/sdk/$path.graphql").default.unsafeCast<String>()
}
