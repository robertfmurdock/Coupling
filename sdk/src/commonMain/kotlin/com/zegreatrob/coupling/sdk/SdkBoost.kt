package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.DeleteBoostCommand
import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.json.JsonBoostRecord
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.json.at
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Boost
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkBoost :
    BoostQuery.Dispatcher,
    GqlSyntax,
    GraphQueries,
    SaveBoostCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: SaveBoostCommand) = doQuery(
        query = mutations.saveBoost,
        input = command.saveBoostInput(),
    ).let { }
        .successResult()

    override suspend fun perform(command: DeleteBoostCommand) = deleteIt().let { Unit.successResult() }

    private fun SaveBoostCommand.saveBoostInput() = SaveBoostInput(partyIds)

    override suspend fun perform(command: BoostQuery) = get()
        ?.successResult()
        ?: NotFoundResult("Boost")

    private suspend fun get() = performer.postAsync(boostQuery()).await()
        .at("/data/user/boost")
        ?.toBoostRecord()

    private fun boostQuery() = buildJsonObject { put("query", queries.boost) }

    private fun JsonElement.toBoostRecord() = fromJsonElement<JsonBoostRecord?>()
        ?.toModelRecord()

    private suspend fun deleteIt() {
        performQuery(buildJsonObject { put("query", mutations.deleteBoost) })
    }

    private fun Boost.saveBoostInput() = SaveBoostInput(partyIds)
}