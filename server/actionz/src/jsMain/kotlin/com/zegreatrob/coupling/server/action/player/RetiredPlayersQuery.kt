package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PartyIdRetiredPlayerRecordsTrait
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class RetiredPlayersQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdRetiredPlayerRecordsTrait {
        suspend fun perform(query: RetiredPlayersQuery): List<PartyRecord<Player>> = query.partyId.loadRetiredPlayerRecords()
            .groupBy { it.data.element.email }
            .flatMap {
                if (it.key.isBlank()) {
                    it.value
                } else {
                    it.value.mergeAllRecordsInGroup()
                }
            }

        private fun List<PartyRecord<Player>>.mergeAllRecordsInGroup(): List<PartyRecord<Player>> = listOfNotNull(
            fold(null) { current, entry ->
                current?.mergeFrom(entry) ?: entry
            },
        )

        private fun PartyRecord<Player>.mergeFrom(entry: PartyRecord<Player>): Record<PartyElement<Player>> = copy(
            data = data.copy(
                element = data.element.copy(
                    additionalEmails = data.element.additionalEmails + entry.data.element.additionalEmails,
                ),
            ),
        )
    }
}
