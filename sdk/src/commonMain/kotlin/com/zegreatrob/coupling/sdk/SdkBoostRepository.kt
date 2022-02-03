package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.BoostRepository
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkBoostRepository : BoostRepository, GqlSyntax {

    override suspend fun get() = performer.postAsync(boostQuery()).await()
        .at("/data/user/boost")
        ?.toBoostRecord()

    private fun boostQuery() = buildJsonObject { put("query", Queries.boost) }

    private fun JsonElement.toBoostRecord() = fromJsonElement<JsonBoostRecord?>()
        ?.toModelRecord()

    override suspend fun delete() {
        performQuery(buildJsonObject { put("query", Mutations.deleteBoost) })
    }

    override suspend fun save(boost: Boost) {
        doQuery(Mutations.saveBoost, boost.saveBoostInput())
    }

    private fun Boost.saveBoostInput() = SaveBoostInput(tribeIds.map(TribeId::value))

}
