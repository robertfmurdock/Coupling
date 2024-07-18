package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlin.time.Duration

@ActionMint
data class PartyContributionQuery(val partyId: PartyId, val window: Duration?, val limit: Int?) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributionQuery): List<PartyRecord<Contribution>> =
            contributionRepository.get(query.partyId, window = query.window, limit = query.limit)
    }
}

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
                        details = playerForEmail(email, players = players),
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
            contributionRepository.get(partyId = this, window = null, limit = null)
                .elements
                .flatMap { it.participantEmails }
                .toSet()
                .sorted()

        private fun playerForEmail(email: String, players: List<PartyRecord<Player>>) = players
            .find { it.element.matches(email) }
    }
}
