package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.json.tribeJsonKeys
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.coupling.sdk.external.axios.AxiosGetEntitySyntax
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface SdkTribeGet : AxiosGetEntitySyntax, TribeGet, AxiosSyntax {
    override suspend fun getTribe(tribeId: TribeId): Tribe = axios.post(
        "/api/graphql", json(
            "query" to "{ tribe(id: \"${tribeId.value}\") {${tribeJsonKeys.joinToString(",")}} }"
        )
    )
        .then<Tribe?> {
            val errors = it.data.errors.unsafeCast<Array<Json>?>()

            if (errors?.isNotEmpty() == true) {
                console.log("errors!!!", errors)
            }

            it.data.unsafeCast<Json>()["data"]
                .unsafeCast<Json>()["tribe"]
                .unsafeCast<Json?>()
                ?.toTribe()
        }
        .await()
        .let { it ?: throw Exception("Tribe not found.") }
}