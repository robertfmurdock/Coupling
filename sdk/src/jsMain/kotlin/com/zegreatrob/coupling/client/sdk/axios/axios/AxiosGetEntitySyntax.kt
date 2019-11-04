package com.zegreatrob.coupling.client.sdk.axios.axios

import kotlin.js.Json
import kotlin.js.Promise

interface AxiosGetEntitySyntax {
    fun Axios.getEntityAsync(url: String): Promise<Json> = get(url)
        .then<dynamic> { result -> result.data.unsafeCast<Json>() }
}