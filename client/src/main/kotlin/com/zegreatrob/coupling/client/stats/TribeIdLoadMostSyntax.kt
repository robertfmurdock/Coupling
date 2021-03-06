package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface TribeIdLoadMostSyntax : TribeIdGetSyntax, TribeIdPlayersSyntax, TribeIdPinsSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

    suspend fun TribeId.loadMost() = coroutineScope {
        await(
            async { get() },
            async { getPlayerList() },
            async { pairAssignmentDocumentRepository.getCurrentPairAssignments(this@loadMost)?.data?.element },
            async { getPins() }
        )
    }

    private suspend fun await(
        tribeDeferred: Deferred<Tribe?>,
        playerListDeferred: Deferred<List<Player>>,
        currentPairsDeferred: Deferred<PairAssignmentDocument?>,
        pinListDeferred: Deferred<List<Pin>>
    ) = tribeDeferred.await()?.let { tribe ->
        awaitTribeData(tribe, playerListDeferred, currentPairsDeferred, pinListDeferred)
    }

    suspend fun awaitTribeData(
        tribe: Tribe,
        playerListDeferred: Deferred<List<Player>>,
        historyDeferred: Deferred<PairAssignmentDocument?>,
        pinListDeferred: Deferred<List<Pin>>
    ) = TribeDataMost(
        tribe,
        playerListDeferred.await(),
        historyDeferred.await(),
        pinListDeferred.await()
    )
}

data class TribeDataMost(
    val tribe: Tribe,
    val playerList: List<Player>,
    val currentPairDocument: PairAssignmentDocument?,
    val pinList: List<Pin>
)