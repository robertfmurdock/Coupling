package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyPinsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface PartyLoadMostSyntax : PartyIdGetSyntax, PartyPlayersSyntax, PartyPinsSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

    suspend fun PartyId.loadMost() = coroutineScope {
        await(
            async { get() },
            async { getPlayerList() },
            async { pairAssignmentDocumentRepository.getCurrentPairAssignments(this@loadMost)?.data?.element },
            async { getPins() },
        )
    }

    private suspend fun await(
        partyDeferred: Deferred<Party?>,
        playerListDeferred: Deferred<List<Player>>,
        currentPairsDeferred: Deferred<PairAssignmentDocument?>,
        pinListDeferred: Deferred<List<Pin>>,
    ) = partyDeferred.await()?.let { party ->
        awaitPartyData(party, playerListDeferred, currentPairsDeferred, pinListDeferred)
    }

    suspend fun awaitPartyData(
        party: Party,
        playerListDeferred: Deferred<List<Player>>,
        historyDeferred: Deferred<PairAssignmentDocument?>,
        pinListDeferred: Deferred<List<Pin>>,
    ) = PartyDataMost(
        party,
        playerListDeferred.await(),
        historyDeferred.await(),
        pinListDeferred.await(),
    )
}

data class PartyDataMost(
    val party: Party,
    val playerList: List<Player>,
    val currentPairDocument: PairAssignmentDocument?,
    val pinList: List<Pin>,
)
