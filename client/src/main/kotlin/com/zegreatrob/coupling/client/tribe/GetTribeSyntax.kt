package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.axios.AxiosGetEntitySyntax
import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toTribe
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface GetTribeSyntax : AxiosGetEntitySyntax {

    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe> = axios.getEntityAsync("/api/tribes/${tribeId.value}")
            .then(Json::toTribe)
            .asDeferred()

}

