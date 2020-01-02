package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerGetter
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PlayerList
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface SdkPlayerGetter : PlayerGetter, TribeGQLSyntax {
    override suspend fun getPlayers(tribeId: TribeId): List<Player> = performTribeGQLQuery(tribeId, listOf(PlayerList))
        .then {
            it[PlayerList].unsafeCast<Array<Json>?>()
                ?.map(Json::toPlayer)
        }
        .await()
        .let { it ?: throw Exception("Tribe not found.") }
}

enum class TribeGQLComponent(val value: String, val jsonPath: String) {
    PlayerList("playerList {${playerJsonKeys.joinToString(",")}}", "/tribe/playerList")
}

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
