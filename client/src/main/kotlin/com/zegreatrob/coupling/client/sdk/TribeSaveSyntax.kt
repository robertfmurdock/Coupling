package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface TribeSaveSyntax {

    fun KtTribe.saveAsync() = axios.post("/api/tribes/", toJson())
        .unsafeCast<Promise<Unit>>()
        .asDeferred()

}
