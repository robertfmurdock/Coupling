package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.recordFor
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.json.tribeRecordJsonKeys
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeListGet
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface SdkTribeListGet : TribeListGet, AxiosSyntax, GqlSyntax {
    override suspend fun getTribes() = axios.post(
        gqlEndpoint, json(
            "query" to "{ tribeList {${tribeRecordJsonKeys.joinToString(",")}} }"
        )
    ).then<dynamic> {
        it.data.unsafeCast<Json>()["data"].unsafeCast<Json>()
            .toTribeRecordList()
    }.await()

    private fun Json.toTribeRecordList(): List<Record<Tribe>> = this["tribeList"]
        .unsafeCast<Array<Json>>()
        .map { it.recordFor(it.toTribe()) }
}