package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.datetime.Instant

@ActionMint
data class PartyContributorQuery(val partyId: PartyId, val contributions: List<Contribution>) {
    interface Dispatcher : PartyIdLoadPlayersTrait {
        suspend fun perform(query: PartyContributorQuery): List<Contributor> {
            val players = query.partyId.loadPlayers()
            return query.contributions.contributorEmails()
                .map { email ->
                    Contributor(
                        email = email,
                        details = playerForEmail(email, players = players, partyId = query.partyId),
                    )
                }
                .groupBy { it.details }
                .flatMap {
                    if (it.key == null) {
                        it.value
                    } else {
                        listOf(
                            Contributor(
                                email = it.key?.element?.email,
                                details = it.key,
                            ),
                        )
                    }
                }
        }

        private fun List<Contribution>.contributorEmails() = flatMap { it.participantEmails }
            .toSet()
            .sorted()

        private fun playerForEmail(
            email: String,
            players: List<PartyRecord<Player>>,
            partyId: PartyId,
        ): Record<PartyElement<Player>> = players
            .find { it.element.matches(email) }
            ?: emptyPlayerDetailsRecord(partyId, email)

        private fun emptyPlayerDetailsRecord(
            partyId: PartyId,
            email: String,
        ) = Record(
            data = PartyElement(
                partyId = partyId,
                element = defaultPlayer.copy(
                    id = partyId.value + email,
                    name = email.substringBefore("@"),
                    email = email,
                ),
            ),
            modifyingUserId = "none",
            isDeleted = false,
            timestamp = Instant.DISTANT_FUTURE,
        )
    }
}
