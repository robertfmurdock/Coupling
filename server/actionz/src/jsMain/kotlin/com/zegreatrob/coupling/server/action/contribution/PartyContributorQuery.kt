package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.datetime.Instant

@ActionMint
data class PartyContributorQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdLoadPlayersTrait {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributorQuery): List<PartyElement<Contributor>> {
            val players = query.partyId.loadPlayers()
            return query.partyId.contributorEmails()
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
                }.map { query.partyId.with(it) }
        }

        private suspend fun PartyId.contributorEmails() =
            contributionRepository.get(ContributionQueryParams(partyId = this, window = null, limit = null))
                .elements
                .flatMap { it.participantEmails }
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
