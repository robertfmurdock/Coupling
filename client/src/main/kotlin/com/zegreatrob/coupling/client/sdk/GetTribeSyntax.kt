package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.AxiosGetEntitySyntax
import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.core.entity.tribe.KtTribe
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.json.toTribe
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface GetTribeSyntax : AxiosGetEntitySyntax {

    fun TribeId.getTribeAsync(): Deferred<KtTribe> = axios.getEntityAsync("/api/tribes/$value")
        .then(Json::toTribe)
        .asDeferred()

}

