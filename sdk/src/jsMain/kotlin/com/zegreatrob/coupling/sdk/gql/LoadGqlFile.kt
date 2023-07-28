package com.zegreatrob.coupling.sdk.gql

actual fun loadGqlFile(path: String): String = kotlinext.js.require<dynamic>("fs")
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
    ?: kotlinext.js.require<dynamic>("com/zegreatrob/coupling/sdk/$path.graphql").default.unsafeCast<String>()
