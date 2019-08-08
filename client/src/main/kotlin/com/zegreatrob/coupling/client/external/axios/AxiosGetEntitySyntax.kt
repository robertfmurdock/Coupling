package com.zegreatrob.coupling.client.external.axios

import kotlin.js.Json
import kotlin.js.Promise

interface AxiosGetEntitySyntax {
    fun Axios.getEntityAsync(url: String): Promise<Json> = get(url)
        .then<dynamic> { result -> result.data.unsafeCast<Json>() }
}