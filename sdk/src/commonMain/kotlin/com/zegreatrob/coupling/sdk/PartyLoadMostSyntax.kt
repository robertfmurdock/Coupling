package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import kotlinx.coroutines.Deferred

interface PartyLoadMostSyntax : SdkProviderSyntax {

    suspend fun PartyId.loadMost(): PartyDataMost? = sdk.perform(
        graphQuery {
            party(this@loadMost) {
                party()
                playerList()
                currentPairAssignments()
                pinList()
            }
        },
    )?.partyData?.let {
        PartyDataMost(
            party = it.party?.data ?: return@let null,
            playerList = it.playerList?.elements ?: return@let null,
            pinList = it.pinList?.elements ?: return@let null,
            currentPairDocument = it.currentPairAssignmentDocument?.element,
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
