package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.json.tribeJsonKeys
import com.zegreatrob.coupling.repository.tribe.TribeListGet
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface SdkTribeListGet : TribeListGet, AxiosSyntax {
    override suspend fun getTribes() = axios.post(
        "/api/graphql", json(
            "query" to "{ tribeList {${tribeJsonKeys.joinToString(",")}} }"
        )
    )
        .then<dynamic> {
            it.data.unsafeCast<Json>()["data"]
                .unsafeCast<Json>()["tribeList"]
                .unsafeCast<Array<Json>>()
                .map(Json::toTribe)
        }
        .await()
}