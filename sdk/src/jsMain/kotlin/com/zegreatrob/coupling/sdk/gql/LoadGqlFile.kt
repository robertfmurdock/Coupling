package com.zegreatrob.coupling.sdk.gql

actual fun loadGqlFile(path: String): String = require<dynamic>("fs")
    ?.readFileSync.unsafeCast<((String, String) -> dynamic)?>()
    ?.let { readFileSync: dynamic ->
        try {
            val dirname = js("__dirname").unsafeCast<String>()
            readFileSync("$dirname/com/zegreatrob/coupling/sdk/$path.graphql", "utf8")
                .unsafeCast<String>()
        } catch (any: Throwable) {
            any.printStackTrace()
            null
        }
    }
    ?: require<dynamic>("com/zegreatrob/coupling/sdk/$path.graphql").default.unsafeCast<String>()

external fun <T> require(module: String): T
