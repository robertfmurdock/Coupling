package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.*
import kotlin.js.Json

interface TribeGQLPerformer : GqlSyntax {

    suspend fun performTribeGQLQuery(
        tribeId: TribeId,
        components: List<TribeGQLComponent>
    ): Map<TribeGQLComponent, dynamic> {
        val result = sendQuery(tribeId, components)
        val data = result.data.data

        return components.associateWith { component ->
            getNodeAtPath(data, component)
        }
    }

    private fun getNodeAtPath(data: dynamic, component: TribeGQLComponent): Any {
        var node = data
        component.jsonPath.split("/").filterNot(String::isBlank).forEach { bit ->
            node = node.unsafeCast<Json?>()?.get(bit)
        }
        return node.unsafeCast<Json>()
    }

    private suspend fun sendQuery(tribeId: TribeId, components: List<TribeGQLComponent>): dynamic =
        buildFinalQuery(tribeId, components)
            .performQuery()

    private fun buildFinalQuery(tribeId: TribeId, components: List<TribeGQLComponent>) =
        "{ ${tribeId.tribeQueryArgs()} { ${components.tribeComponentString()} } }"

    private fun TribeId.tribeQueryArgs() = "tribeData(id: \"$value\")"

    private fun List<TribeGQLComponent>.tribeComponentString() = joinToString(",") { it.value }

}

class BatchingTribeGQLPerformer(override val performer: QueryPerformer) : TribeGQLPerformer {

    private val batchScope = MainScope() + CoroutineName("batch")

    private var pending: Deferred<Map<TribeGQLComponent, dynamic>>? = null

    private var pendingComponents = emptyList<TribeGQLComponent>()

    override suspend fun performTribeGQLQuery(
        tribeId: TribeId,
        components: List<TribeGQLComponent>
    ): Map<TribeGQLComponent, dynamic> {
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