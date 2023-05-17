package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface PartyLoadAllSyntax : SdkProviderSyntax, PartyIdHistorySyntax {
    suspend fun PartyId.loadAll() = coroutineScope {
        await(
            async { sdk.getPartyRecord(this@loadAll)?.data },
            async { sdk.getPlayers(this@loadAll).elements },
            async { loadHistory() },
            async {
                sdk.perform(graphQuery { party(this@loadAll) { pinList() } })
                    ?.partyData
                    ?.pinList
                    ?.elements
                    ?: emptyList()
            },
        )
    }

    private suspend fun await(
        partyDeferred: Deferred<Party?>,
        playerListDeferred: Deferred<List<Player>>,
        historyDeferred: Deferred<List<PairAssignmentDocument>>,
        pinListDeferred: Deferred<List<Pin>>,
    ): PartyData? {
        return awaitPartyData(
            (partyDeferred.await() ?: return null),
            playerListDeferred,
            historyDeferred,
            pinListDeferred,
        )
    }

    suspend fun awaitPartyData(
        party: Party,
        playerListDeferred: Deferred<List<Player>>,
        historyDeferred: Deferred<List<PairAssignmentDocument>>,
        pinListDeferred: Deferred<List<Pin>>,
    ) = PartyData(
        party,
        playerListDeferred.await(),
        historyDeferred.await(),
        pinListDeferred.await(),
    )
}

data class PartyData(
    val party: Party,
    val playerList: List<Player>,
    val history: List<PairAssignmentDocument>,
    val pinList: List<Pin>,
)
