package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PartyContributorQuery(val partyId: PartyId, val contributions: List<Contribution>) {
    interface Dispatcher : PartyIdLoadPlayersTrait {
        suspend fun perform(query: PartyContributorQuery): List<Contributor> = emailsToContributors(
            partyId = query.partyId,
            contributorEmails = query.contributions.contributorEmails(),
            players = query.partyId.loadPlayers(),
        )

        private fun emailsToContributors(
            partyId: PartyId,
            contributorEmails: List<String>,
            players: List<PartyRecord<Player>>,
        ): List<Contributor> = contributorEmails.map { email -> contributor(partyId, email, players) }
            .groupBy(Contributor::details)
            .flatMap {
                it.key?.let { details -> listOf(Contributor(email = details.element.email, details = details)) }
                    ?: it.value
            }

        private fun contributor(
            partyId: PartyId,
            email: String,
            players: List<PartyRecord<Player>>,
        ): Contributor = Contributor(
            email = email,
            details = playerForEmail(email, players = players, partyId = partyId),
        )

        private fun List<Contribution>.contributorEmails() = flatMap { it.participantEmails }.toSet().sorted()

        private fun playerForEmail(
            email: String,
            players: List<PartyRecord<Player>>,
            partyId: PartyId,
        ): PartyRecord<Player>? = players.find { it.element.matches(email) }
    }
}
