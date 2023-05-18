package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.graphQuery
import kotlinx.coroutines.Deferred

interface PartyLoadAllSyntax : SdkProviderSyntax {
    suspend fun PartyId.loadAll(): PartyData? = sdk.perform(
        graphQuery {
            party(this@loadAll) {
                party()
                playerList()
                pairAssignmentDocumentList()
                pinList()
            }
        },
    )?.partyData?.let {
        PartyData(
            party = it.party?.data ?: return@let null,
            playerList = it.playerList?.elements ?: return@let null,
            history = it.pairAssignmentDocumentList?.elements ?: return@let null,
            pinList = it.pinList?.elements ?: return@let null,
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
