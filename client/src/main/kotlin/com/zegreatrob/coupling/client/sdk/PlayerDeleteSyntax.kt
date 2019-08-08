package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface PlayerDeleteSyntax {

    fun deleteAsync(tribeId: TribeId, playerId: String) = axios.delete("/api/${tribeId.value}/players/$playerId")
        .unsafeCast<Promise<Unit>>()
        .asDeferred()

}
