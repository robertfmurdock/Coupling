package com.zegreatrob.coupling.sdk.gql

actual fun loadGqlFile(path: String): String =
    Loader::class.java.classLoader.getResourceAsStream("/com/zegreatrob/coupling/sdk/$path")
        ?.readAllBytes()
        .toString()

private object Loader
