package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.external.axios.Axios
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred

interface AxiosSyntax {
    val axios: Axios get() = com.zegreatrob.coupling.sdk.external.axios.axios

    fun <T> Axios.postAsync(url: String, body: dynamic): Deferred<T> = post(url, body)
        .then<T> { it.data.unsafeCast<T>() }
        .asDeferred()
        .unsafeCast<Deferred<T>>()
}
