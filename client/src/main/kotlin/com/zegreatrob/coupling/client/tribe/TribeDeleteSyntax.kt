package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface TribeIdDeleteSyntax {

    fun TribeId.deleteAsync() = axios.delete("/api/tribes/$value")
        .unsafeCast<Promise<Unit>>()
        .asDeferred()

}
