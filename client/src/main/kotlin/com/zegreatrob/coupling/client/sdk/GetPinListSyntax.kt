package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.core.entity.pin.Pin
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPins
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred

interface GetPinListSyntax {

    fun TribeId.getPinListAsync(): Deferred<List<Pin>> = axios.getList("/api/$value/pins")
        .then { it.toPins() }
        .asDeferred()

}