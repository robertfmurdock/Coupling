package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import kotlinx.coroutines.*
import kotlin.js.Json
import kotlin.js.json

interface TribeGQLSyntax : AxiosSyntax {

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
        return node
    }

    private suspend fun sendQuery(tribeId: TribeId, components: List<TribeGQLComponent>): dynamic = axios.post(
        "/api/graphql", json(
            "query" to "{ ${tribeId.tribeQueryArgs()} { ${components.tribeComponentString()} } }"
        )
    ).await()

    private fun TribeId.tribeQueryArgs() = "tribe(id: \"$value\")"

    private fun List<TribeGQLComponent>.tribeComponentString() = joinToString(",") { it.value }

}

class BatchingTribeGQLSyntax(override val axios: Axios) : TribeGQLSyntax {

    val batchScope = MainScope() + CoroutineName("batch")

    var pendingComponents = emptyList<TribeGQLComponent>()

    var pending: Deferred<Map<TribeGQLComponent, dynamic>>? = null

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