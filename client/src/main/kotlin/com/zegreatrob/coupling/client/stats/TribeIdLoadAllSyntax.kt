package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface TribeIdLoadAllSyntax : TribeIdGetSyntax, TribeIdPlayersSyntax, TribeIdHistorySyntax, TribeIdPinsSyntax {
    suspend fun PartyId.loadAll() = coroutineScope {
        await(
            async { get() },
            async { getPlayerList() },
            async { loadHistory() },
            async { getPins() }
        )
    }

    private suspend fun await(
        tribeDeferred: Deferred<Party?>,
        playerListDeferred: Deferred<List<Player>>,
        historyDeferred: Deferred<List<PairAssignmentDocument>>,
        pinListDeferred: Deferred<List<Pin>>
    ): TribeData? {
        return awaitTribeData(
            (tribeDeferred.await() ?: return null),
            playerListDeferred,
            historyDeferred,
            pinListDeferred
        )
    }

    suspend fun awaitTribeData(
        tribe: Party,
        playerListDeferred: Deferred<List<Player>>,
        historyDeferred: Deferred<List<PairAssignmentDocument>>,
        pinListDeferred: Deferred<List<Pin>>
    ) = TribeData(
        tribe,
        playerListDeferred.await(),
        historyDeferred.await(),
        pinListDeferred.await()
    )
}

data class TribeData(
    val tribe: Party,
    val playerList: List<Player>,
    val history: List<PairAssignmentDocument>,
    val pinList: List<Pin>
)

