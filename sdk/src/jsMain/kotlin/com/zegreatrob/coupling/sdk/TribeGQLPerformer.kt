package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import kotlinx.coroutines.*
import kotlin.js.Json
import kotlin.js.json

interface TribeGQLPerformer : AxiosSyntax {

    suspend fun performTribeGQLQuery(
        tribeId: TribeId,
        components: List<TribeGQLComponent>
    ): Map<TribeGQLComponent, dynamic> {
        val result = sendQuery(tribeId, components)
        val data = result.data.data

        return components.map { component ->
            val node = getNodeAtPath(data, component)
            component to node
        }.toMap()
    }

    private fun getNodeAtPath(data: dynamic, component: TribeGQLComponent): Any? {
        var node = data
        component.jsonPath.split("/").filterNot(String::isBlank).forEach { bit ->
            node = node.unsafeCast<Json?>()?.get(bit)
        }
        return node.unsafeCast<Json>()
    }

    private suspend fun sendQuery(tribeId: TribeId, components: List<TribeGQLComponent>): dynamic = axios.post(
        "/api/graphql", json(
            "query" to "{ ${tribeId.tribeQueryArgs()} { ${components.tribeComponentString()} } }"
        )
    ).await()

    private fun TribeId.tribeQueryArgs() = "tribe(id: \"$value\")"

    private fun List<TribeGQLComponent>.tribeComponentString() = joinToString(",") { it.value }

}

class BatchingTribeGQLPerformer(override val axios: Axios) : TribeGQLPerformer {

    private val batchScope = MainScope() + CoroutineName("batch")

    private var pending: Deferred<Map<TribeGQLComponent, dynamic>>? = null

    var pendingComponents = emptyList<TribeGQLComponent>()

    override suspend fun performTribeGQLQuery(
        tribeId: TribeId,
        components: List<TribeGQLComponent>
    ): Map<TribeGQLComponent, dynamic> {
        pendingComponents += components

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