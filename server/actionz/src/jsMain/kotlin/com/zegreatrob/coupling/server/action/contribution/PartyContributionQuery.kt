package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PartyContributionQuery(val partyId: PartyId) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributionQuery): List<PartyRecord<Contribution>> =
            contributionRepository.get(query.partyId)
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
            contributionRepository.get(this)
                .elements
                .flatMap { it.participantEmails }
                .toSet()
                .sorted()

        private fun playerForEmail(email: String, players: List<PartyRecord<Player>>) = players
            .find {
                listOf(it.element.email)
                    .plus(it.element.additionalEmails)
                    .map(String::lowercase)
                    .contains(email.lowercase())
            }
    }
}

@ActionMint
data class ContributorPlayerQuery(val partyId: PartyId, val email: String) {
    interface Dispatcher : PartyPlayersSyntax {
        suspend fun perform(query: ContributorPlayerQuery): PartyRecord<Player>? = query.partyId.loadPlayers()
            .find {
                listOf(it.element.email)
                    .plus(it.element.additionalEmails)
                    .map(String::lowercase)
                    .contains(query.email)
            }
    }
}

@ActionMint
data class PairContributionQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PairContributionQuery): List<PartyRecord<Contribution>> {
            val targetEmails = query.pair.asArray().mapNotNull { it.email.ifEmpty { null } }.toSet()
            return contributionRepository.get(query.partyId)
                .filter { it.data.element.participantEmails == targetEmails }
        }
    }
}
