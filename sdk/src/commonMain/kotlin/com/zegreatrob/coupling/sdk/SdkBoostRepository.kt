package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.json.JsonBoostRecord
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.json.at
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.repository.BoostRepository
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkBoostRepository : BoostRepository, GqlSyntax, GraphQueries, SaveBoostCommand.Dispatcher {

    override suspend fun perform(command: SaveBoostCommand) = doQuery(
        query = mutations.saveBoost,
        input = command.saveBoostInput(),
    ).let { }
        .successResult()

    private fun SaveBoostCommand.saveBoostInput() = SaveBoostInput(partyIds)

    override suspend fun get() = performer.postAsync(boostQuery()).await()
        .at("/data/user/boost")
        ?.toBoostRecord()

    private fun boostQuery() = buildJsonObject { put("query", queries.boost) }

    private fun JsonElement.toBoostRecord() = fromJsonElement<JsonBoostRecord?>()
        ?.toModelRecord()

    override suspend fun deleteIt() {
        performQuery(buildJsonObject { put("query", mutations.deleteBoost) })
    }

    override suspend fun save(boost: Boost) {
        doQuery(mutations.saveBoost, boost.saveBoostInput())
    }

    private fun Boost.saveBoostInput() = SaveBoostInput(partyIds)
}
