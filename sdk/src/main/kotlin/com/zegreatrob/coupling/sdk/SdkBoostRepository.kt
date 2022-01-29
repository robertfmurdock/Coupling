package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonBoostRecord
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.BoostRepository
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json
import kotlin.js.json

interface SdkBoostRepository : BoostRepository, GqlSyntax {

    override suspend fun get() = performer.postAsync(boostQuery()).await()
        .at<Json>("/data/user/boost")
        ?.toBoostRecord()

    private fun boostQuery() = json("query" to Queries.boost)

    private fun Json.toBoostRecord(): Record<Boost> = couplingJsonFormat.decodeFromDynamic<JsonBoostRecord>(this)
        .toModelRecord()

    override suspend fun delete() = performQuery(json("query" to Mutations.deleteBoost))

    override suspend fun save(boost: Boost) = doQuery(Mutations.saveBoost, boost.saveBoostInput())
        .unsafeCast<Unit>()

    private fun Boost.saveBoostInput() = SaveBoostInput(id, tribeIds.map(TribeId::value))

}
