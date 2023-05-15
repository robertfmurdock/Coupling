package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface PartyLoadMostSyntax : SdkProviderSyntax, PartyPlayersSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetCurrent

    suspend fun PartyId.loadMost() = coroutineScope {
        await(
            async { sdk.getPartyRecord(this@loadMost)?.data },
            async { getPlayerList() },
            async { pairAssignmentDocumentRepository.getCurrentPairAssignments(this@loadMost)?.data?.element },
            async { sdk.getPins(this@loadMost).elements },
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
