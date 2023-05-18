package com.zegreatrob.coupling.sdk

actual fun loadGqlFile(path: String): String =
    Loader::class.java.classLoader.getResourceAsStream("/com/zegreatrob/coupling/sdk/$path")
        ?.readAllBytes()
        .toString()

private object Loader
