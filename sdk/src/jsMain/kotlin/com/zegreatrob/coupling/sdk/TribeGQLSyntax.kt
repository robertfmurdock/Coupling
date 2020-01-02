package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json
import kotlin.js.json

interface TribeGQLSyntax : AxiosSyntax {

    fun performTribeGQLQuery(tribeId: TribeId, components: List<TribeGQLComponent>) = axios.post(
        "/api/graphql", json(
            "query" to "{ tribe(id: \"${tribeId.value}\") { ${components.joinToString(",") { it.value }} } }"
        )
    ).then<Map<TribeGQLComponent, dynamic>> {
        val data = it.data.data

        components.map { component ->
            var node = data
            component.jsonPath.split("/").filterNot(String::isBlank).forEach { bit ->
                node = node.unsafeCast<Json?>()?.get(bit)
            }
            component to node
        }.toMap()
    }

}