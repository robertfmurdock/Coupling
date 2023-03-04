package com.zegreatrob.coupling.sdk

actual fun loadGqlFile(path: String): String = kotlinext.js.require("fs")
    ?.readFileSync.unsafeCast<((String, String) -> dynamic)?>()
    ?.let { readFileSync ->
        try {
            val dirname = js("__dirname").unsafeCast<String>()
            readFileSync("$dirname/com/zegreatrob/coupling/sdk/$path.graphql", "utf8")
                .unsafeCast<String>()
        } catch (any: Throwable) {
            any.printStackTrace()
            null
        }
    }
    ?: kotlinext.js.require("com/zegreatrob/coupling/sdk/$path.graphql").default.unsafeCast<String>()
