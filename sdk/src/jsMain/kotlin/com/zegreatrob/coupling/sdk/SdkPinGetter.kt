package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPins
import com.zegreatrob.coupling.model.pin.PinGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.asDeferred

interface SdkPinGetter : PinGetter, AxiosSyntax {
    override fun getPinsAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/pins")
        .then { it.toPins() }
        .asDeferred()
}