package com.zegreatrob.coupling.sdk.external.axios

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface AxiosGetEntitySyntax {
    fun Axios.getEntityAsync(url: String): Deferred<Json> = get(url)
        .then<dynamic> { result -> result.data.unsafeCast<Json>() }
        .asDeferred()
}