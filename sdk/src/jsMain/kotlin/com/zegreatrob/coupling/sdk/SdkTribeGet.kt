package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeGet
import kotlin.js.Json

interface SdkTribeGet : TribeGet, TribeGQLSyntax {
    override suspend fun getTribe(tribeId: TribeId): Tribe =
        performTribeGQLQuery(tribeId, listOf(TribeGQLComponent.Tribe))
            .let {
                it[TribeGQLComponent.Tribe].unsafeCast<Json?>()
                    ?.toTribe()
                    ?: throw Exception("Tribe not found.")
            }
}