package com.zegreatrob.coupling.sdk

import kotlin.js.Json

actual fun loadTextFile(path: String) : String = kotlinext.js.require("fs")
    ?.readFileSync.unsafeCast<((String, String) -> dynamic)?>()
    ?.let { readFileSync ->

        console.log("readFileSync exists, now we look at paths")

        val nodePaths = js("process.env").unsafeCast<Json>()["NODE_PATH"].unsafeCast<String?>()
            ?.split(":") ?: emptyList()
        (nodePaths + "${js("__dirname")}")
            .asSequence()
            .mapNotNull { nodePath ->
                try {
                    readFileSync("$nodePath/com/zegreatrob/coupling/sdk/$path.graphql", "utf8")
                        .unsafeCast<String?>()
                } catch (any: Throwable) {
                    console.log("we're checking ", nodePath, "but got", any.message)
                    null
                }
            }
            .first()
    }
    ?: kotlinext.js.require("com/zegreatrob/coupling/sdk/$path.graphql").default.unsafeCast<String>()