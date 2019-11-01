package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.json.toTribe
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface GetTribeListSyntax {

    fun getTribeListAsync(): Deferred<List<KtTribe>> = axios.getList("/api/tribes")
        .then { it.map(Json::toTribe) }
        .asDeferred()

}