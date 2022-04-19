package com.zegreatrob.coupling.sdk

import kotlin.js.Json

actual fun loadGqlFile(path: String): String = kotlinext.js.require("fs")
    ?.readFileSync.unsafeCast<((String, String) -> dynamic)?>()
    ?.let { readFileSync ->
        val nodePaths = js("process.env").unsafeCast<Json>()["NODE_PATH"].unsafeCast<String?>()
            ?.split(":") ?: emptyList()
        (nodePaths + "${js("__dirname")}")
            .asSequence()
            .mapNotNull { nodePath ->
                try {
                    readFileSync("$nodePath/com/zegreatrob/coupling/sdk/$path.graphql", "utf8")
                        .unsafeCast<String?>()
                } catch (any: Throwable) {
                    null
                }
            }
            .first()
    }
    ?: kotlinext.js.require("com/zegreatrob/coupling/sdk/$path.graphql").default.unsafeCast<String>()
