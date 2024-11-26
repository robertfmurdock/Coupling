package com.zegreatrob.coupling.sdk.gql

import java.nio.charset.Charset

actual fun loadGqlFile(path: String): String = Loader::class.java.classLoader.getResourceAsStream("com/zegreatrob/coupling/sdk/$path.graphql")
    ?.readAllBytes()
    ?.toString(Charset.forName("UTF-8"))
    ?: ""

private object Loader
