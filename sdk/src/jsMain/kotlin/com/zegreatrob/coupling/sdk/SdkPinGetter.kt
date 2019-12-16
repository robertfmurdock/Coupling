package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pin.PinGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.await

interface SdkPinGetter : PinGetter, AxiosSyntax {
    override suspend fun getPins(tribeId: TribeId): List<Pin> = axios.getList("/api/tribes/${tribeId.value}/pins")
        .then { it.toPins() }
        .await()
}