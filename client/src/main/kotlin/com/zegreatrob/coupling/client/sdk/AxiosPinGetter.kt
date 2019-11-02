package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.json.toPins
import com.zegreatrob.coupling.model.pin.PinGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred

interface AxiosPinGetter : PinGetter {
    override fun getPinsAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/pins")
        .then { it.toPins() }
        .asDeferred()
}