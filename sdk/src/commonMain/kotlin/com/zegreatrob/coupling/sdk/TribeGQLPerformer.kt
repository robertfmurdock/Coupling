package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

interface TribeGQLPerformer : GqlSyntax {

    suspend fun performTribeGQLQuery(
        tribeId: TribeId,
        components: List<TribeGQLComponent>
    ): Map<TribeGQLComponent, JsonElement?> {
        val result = sendQuery(tribeId, components)
        val data = result.jsonObject["data"]

        return components.associateWith { component ->
            getNodeAtPath(data, component)
        }
    }

    private fun getNodeAtPath(data: JsonElement?, component: TribeGQLComponent): JsonElement? {
        var node: JsonElement? = data
        component.jsonPath.split("/").filterNot(String::isBlank).forEach { bit ->
            node = node?.jsonObject?.get(bit)
        }
        return node
    }

    private suspend fun sendQuery(tribeId: TribeId, components: List<TribeGQLComponent>) =
        buildFinalQuery(tribeId, components)
            .performQuery()

    private fun buildFinalQuery(tribeId: TribeId, components: List<TribeGQLComponent>) =
        "{ ${tribeId.tribeQueryArgs()} { ${components.tribeComponentString()} } }"

    private fun TribeId.tribeQueryArgs() = "tribeData(id: \"$value\")"

    private fun List<TribeGQLComponent>.tribeComponentString() = joinToString(",") { it.value }

}

class BatchingTribeGQLPerformer(override val performer: QueryPerformer) : TribeGQLPerformer {

    private val batchScope = MainScope() + CoroutineName("batch")

    private var pending: Deferred<Map<TribeGQLComponent, JsonElement?>>? = null

    private var pendingComponents = emptyList<TribeGQLComponent>()

    override suspend fun performTribeGQLQuery(
        tribeId: TribeId,
        components: List<TribeGQLComponent>
    ): Map<TribeGQLComponent, JsonElement?> {
        pendingComponents = pendingComponents + components

        return with(batchScope) {
            if (pending == null) {
                val deferred = async {
                    super.performTribeGQLQuery(tribeId, pendingComponents)
                        .also { pendingComponents = emptyList(); pending = null }
                }
                pending = deferred
                deferred
            } else {
                pending!!
            }
        }.await()
    }

}