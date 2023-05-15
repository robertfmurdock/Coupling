package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.CouplingQueryResult
import com.zegreatrob.coupling.json.JsonCouplingQueryResult
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

interface PartyGQLPerformer : GqlSyntax {

    suspend fun performPartyGQLQuery(
        partyId: PartyId,
        components: List<PartyGQLComponent>,
    ) = sendQuery(partyId, components).jsonObject["data"]
        ?.let<JsonElement, JsonCouplingQueryResult>(Json.Default::decodeFromJsonElement)
        ?.toDomain()

    private suspend fun sendQuery(partyId: PartyId, components: List<PartyGQLComponent>) =
        buildFinalQuery(partyId, components)
            .performQuery()

    private fun buildFinalQuery(partyId: PartyId, components: List<PartyGQLComponent>) =
        "{ ${partyId.partyQueryArgs()} { ${components.partyComponentString()} } }"

    private fun PartyId.partyQueryArgs() = "partyData(id: \"$value\")"

    private fun List<PartyGQLComponent>.partyComponentString() = joinToString(",") { it.value }
}

class BatchingPartyGQLPerformer(override val performer: QueryPerformer) : PartyGQLPerformer {

    private val batchScope = MainScope() + CoroutineName("batch")

    private var pending: Deferred<CouplingQueryResult?>? = null

    private var pendingComponents = emptyList<PartyGQLComponent>()

    override suspend fun performPartyGQLQuery(
        partyId: PartyId,
        components: List<PartyGQLComponent>,
    ): CouplingQueryResult? {
        pendingComponents = pendingComponents + components

        return with(batchScope) {
            if (pending == null) {
                val deferred = async {
                    super.performPartyGQLQuery(partyId, pendingComponents)
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
